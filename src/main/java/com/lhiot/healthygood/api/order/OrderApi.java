package com.lhiot.healthygood.api.order;

import com.leon.microx.predefine.OnOff;
import com.leon.microx.util.BeanUtils;
import com.leon.microx.util.Calculator;
import com.leon.microx.util.StringUtils;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.leon.microx.web.result.Tuple;
import com.leon.microx.web.session.Sessions;
import com.lhiot.healthygood.config.HealthyGoodConfig;
import com.lhiot.healthygood.domain.user.DoctorUser;
import com.lhiot.healthygood.domain.user.FruitDoctor;
import com.lhiot.healthygood.feign.BaseDataServiceFeign;
import com.lhiot.healthygood.feign.OrderServiceFeign;
import com.lhiot.healthygood.feign.PaymentServiceFeign;
import com.lhiot.healthygood.feign.ThirdpartyServerFeign;
import com.lhiot.healthygood.feign.model.*;
import com.lhiot.healthygood.feign.type.*;
import com.lhiot.healthygood.service.order.OrderService;
import com.lhiot.healthygood.service.user.DoctorUserService;
import com.lhiot.healthygood.service.user.FruitDoctorService;
import com.lhiot.healthygood.util.ConvertRequestToMap;
import com.lhiot.healthygood.util.FeginResponseTools;
import com.lhiot.healthygood.util.RealClientIp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Api(description = "普通订单接口")
@Slf4j
@RestController
public class OrderApi {
    private final BaseDataServiceFeign baseDataServiceFeign;
    private final ThirdpartyServerFeign thirdpartyServerFeign;
    private final OrderServiceFeign orderServiceFeign;
    private final PaymentServiceFeign paymentServiceFeign;
    private final OrderService orderService;
    private final DoctorUserService doctorUserService;
    private final FruitDoctorService fruitDoctorService;
    private final HealthyGoodConfig.WechatPayConfig wechatPayConfig;

    @Autowired
    public OrderApi(BaseDataServiceFeign baseDataServiceFeign,
                    ThirdpartyServerFeign thirdpartyServerFeign,
                    OrderServiceFeign orderServiceFeign,
                    PaymentServiceFeign paymentServiceFeign,
                    OrderService orderService,
                    DoctorUserService doctorUserService, FruitDoctorService fruitDoctorService, HealthyGoodConfig healthyGoodConfig) {
        this.baseDataServiceFeign = baseDataServiceFeign;
        this.thirdpartyServerFeign = thirdpartyServerFeign;
        this.orderServiceFeign = orderServiceFeign;
        this.paymentServiceFeign = paymentServiceFeign;
        this.orderService = orderService;
        this.doctorUserService = doctorUserService;
        this.fruitDoctorService = fruitDoctorService;
        this.wechatPayConfig = healthyGoodConfig.getWechatPay();
    }

    @PostMapping("/orders")
    @ApiOperation("和色果膳--创建订单")
    @ApiImplicitParam(paramType = "body", name = "orderParam", dataType = "CreateOrderParam", required = true, value = "创建订单传入参数")
    public ResponseEntity<Tips> createOrder(@Valid @RequestBody CreateOrderParam orderParam, Sessions.User user) {

        Map<String, Object> sessionUserMap = user.getUser();
        Long userId = Long.valueOf(sessionUserMap.get("userId").toString());
        orderParam.setUserId(userId);//设置业务用户id
        orderParam.setApplicationType(ApplicationType.HEALTH_GOOD);
        orderParam.setOrderType(OrderType.NORMAL);//普通订单

        String storeCode = orderParam.getOrderStore().getStoreCode();
        //判断门店是否存在
        ResponseEntity<Store> storeResponseEntity = baseDataServiceFeign.findStoreByCode(storeCode);
        Tips<Store> storeTips = FeginResponseTools.convertResponse(storeResponseEntity);
        if (storeTips.err()) {
            return ResponseEntity.badRequest().body(storeTips);
        }
        Store store = storeTips.getData();

        //给订单门店赋值
        OrderStore orderStore = new OrderStore();
        orderStore.setStoreId(store.getId());
        orderStore.setStoreCode(store.getCode());
        orderStore.setStoreName(store.getName());
        orderParam.setOrderStore(orderStore);

        List<OrderProduct> orderProducts = orderParam.getOrderProducts();
        //依据上架ids查询上架商品信息
        Object[] shelfIds = orderProducts.parallelStream().map(OrderProduct::getShelfId).toArray(Object[]::new);

        ProductShelfParam productShelfParam = new ProductShelfParam();
        productShelfParam.setIds(StringUtils.arrayToCommaDelimitedString(shelfIds));
        productShelfParam.setShelfStatus(OnOff.ON);
        //查找基础服务上架商品信息
        Tips<Pages<ProductShelf>> productShelfTips = FeginResponseTools.convertResponse(baseDataServiceFeign.searchProductShelves(productShelfParam));
        if (productShelfTips.err()) {
            return ResponseEntity.badRequest().body(productShelfTips);
        }
        Pages<ProductShelf> productShelfPages = productShelfTips.getData();
        String[] barcodes = productShelfPages.getArray().parallelStream()
                .map(ProductShelf::getProductSpecification)
                .map(ProductSpecification::getBarcode).toArray(String[]::new);

        //查询门店h4库存信息
        Tips<Map<String, Object>> quserSkuTips = FeginResponseTools.convertResponse(thirdpartyServerFeign.querySku(orderParam.getOrderStore().getStoreCode(), barcodes));

        if (quserSkuTips.err()) {
            return ResponseEntity.badRequest().body(quserSkuTips);
        }
        List<Map> businvs = (List<Map>) quserSkuTips.getData().get("businvs");
        //校验库存是否足够
        for (ProductShelf productShelf : productShelfPages.getArray()) {
            for (Map businv : businvs) {
                if (Objects.equals(productShelf.getProductSpecification().getBarcode(), businv.get("barCode"))
                        && productShelf.getProductSpecification().getLimitInventory() > Double.valueOf(businv.get("qty").toString())) {
                    return ResponseEntity.badRequest().body(
                            Tips.warn(String.format("%s实际库存%s低于安全库存%d，不允许销售", productShelf.getProductSpecification().getBarcode(), businv.get("qty").toString(), productShelf.getProductSpecification().getLimitInventory()))
                    );
                }
            }
        }

        //给商品赋值规格数量
        orderParam.getOrderProducts().forEach(orderProduct -> productShelfPages.getArray().stream()
                //上架id相同的订单商品信息，通过基础服务获取的赋值给订单商品信息
                .filter(productShelf -> Objects.equals(orderProduct.getShelfId(), productShelf.getId()))
                .forEach(item -> {
                    BeanUtils.copyProperties(item, orderProduct);
                    //设置规格信息
                    ProductSpecification productSpecification = item.getProductSpecification();
                    orderProduct.setBarcode(productSpecification.getBarcode());
                    orderProduct.setTotalWeight(productSpecification.getWeight());
                    orderProduct.setSpecificationId(productSpecification.getId());
                    //设置上架信息
                    //如果存在特价就用特价
                    Integer price = Objects.isNull(item.getPrice()) ? item.getOriginalPrice() : item.getPrice();
                    orderProduct.setDiscountPrice((int) Calculator.mul(price, orderProduct.getProductQty()));//去除优惠有单品总价
                    orderProduct.setTotalPrice((int) Calculator.mul(item.getOriginalPrice(), orderProduct.getProductQty()));//单品总价
                    orderProduct.setId(null);
                    orderProduct.setProductName(item.getName());
                })
        );
        ResponseEntity<OrderDetailResult> orderDetailResultResponse = orderServiceFeign.createOrder(orderParam);
        Tips<OrderDetailResult> orderDetailResultTips = FeginResponseTools.convertResponse(orderDetailResultResponse);
        if (orderDetailResultTips.err()) {
            return ResponseEntity.badRequest().body(orderDetailResultTips);
        }
        //TODO mq设置三十分钟失效
        return ResponseEntity.ok(orderDetailResultTips);
    }

    @DeleteMapping("/orders/{orderCode}")
    @ApiOperation("和色果膳--取消订单")
    @ApiImplicitParam(paramType = "path", name = "orderCode", dataType = "String", required = true, value = "订单id")
    public ResponseEntity<Tips> createOrder(@Valid @NotBlank @PathVariable("orderCode") String orderCode, Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        ResponseEntity<Tips> validateResult = validateOrderOwner(userId, orderCode);
        if (Objects.isNull(validateResult) || validateResult.getStatusCode().isError()) {
            return validateResult;
        }
        Tips updateOrderTips = FeginResponseTools.convertResponse(orderServiceFeign.updateOrderStatus(orderCode, OrderStatus.FAILURE));
        return FeginResponseTools.returnTipsResponse(updateOrderTips);
    }


    @PutMapping("/orders/{orderCode}/refund")
    @ApiOperation("订单退货")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "orderCode", dataType = "String", required = true, value = "订单编码"),
            @ApiImplicitParam(paramType = "body", name = "returnOrderParam", dataType = "ReturnOrderParam", required = true, value = "退货参数")
    })
    public ResponseEntity<Tips> refundOrder(@Valid @NotBlank @PathVariable("orderCode") String orderCode,
                                            @NotNull @RequestBody ReturnOrderParam returnOrderParam,
                                            Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        ResponseEntity<Tips> validateResult = validateOrderOwner(userId, orderCode);
        if (Objects.isNull(validateResult) || validateResult.getStatusCode().isError()) {
            return validateResult;
        }
        OrderDetailResult orderDetailResult = (OrderDetailResult) validateResult.getBody().getData();
        Tips refundOrderTips = null;
        switch (orderDetailResult.getStatus()) {
            //订单未发送海鼎，退款
            case WAIT_SEND_OUT:
                refundOrderTips = FeginResponseTools.convertResponse(orderServiceFeign.notSendHdRefundOrder(orderCode, returnOrderParam));
                break;
            //海鼎备货后提交退货
            case WAIT_DISPATCHING:
                refundOrderTips = FeginResponseTools.convertResponse(orderServiceFeign.returnsRefundOrder(orderCode, returnOrderParam));
                break;
            //已收货
            case RECEIVED:
                refundOrderTips = FeginResponseTools.convertResponse(orderServiceFeign.returnsRefundOrder(orderCode, returnOrderParam));
                break;
            //订单发送海鼎，未备货退货
            case SEND_OUTING:
                refundOrderTips = FeginResponseTools.convertResponse(orderServiceFeign.sendHdRefundOrder(orderCode, returnOrderParam));
                break;

        }

        return FeginResponseTools.returnTipsResponse(refundOrderTips);
    }

    @GetMapping("/orders/pages")
    @ApiOperation("我的用户订单列表")
    public ResponseEntity<Tips> orderPages(@RequestParam(value = "orderStatus", required = false) OrderStatus orderStatus,
                                           Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        Tips<Tuple<OrderDetailResult>> ordersByUserIdTips = FeginResponseTools.convertResponse(orderServiceFeign.ordersByUserId(userId, OrderType.NORMAL, orderStatus));
        return FeginResponseTools.returnTipsResponse(ordersByUserIdTips);
    }

    @GetMapping("/orders/fruit-doctor/customers")
    @ApiOperation("我的鲜果师客户订单列表")
    public ResponseEntity<Tips> customersOrders(@RequestParam(value = "orderStatus", required = false) OrderStatus orderStatus, Sessions.User user) {
        String userId = user.getUser().get("userId").toString();
        FruitDoctor fruitDoctor = fruitDoctorService.selectByUserId(Long.valueOf(userId));
        if (Objects.isNull(fruitDoctor)) {
            return ResponseEntity.badRequest().body(Tips.warn("鲜果师不存在"));
        }
        //鲜果师客户列表
        List<DoctorUser> doctorUserList = doctorUserService.selectByDoctorId(fruitDoctor.getId());

        String userIds = StringUtils.arrayToCommaDelimitedString(doctorUserList.parallelStream().map(DoctorUser::getUserId).toArray(Object[]::new));
        BaseOrderParam baseOrderParam = new BaseOrderParam();
        baseOrderParam.setUserIds(userIds);
        baseOrderParam.setOrderStatus(orderStatus);
        baseOrderParam.setOrderType(OrderType.NORMAL);
        Tips<Pages<OrderDetailResult>> orderListTips = FeginResponseTools.convertResponse(orderServiceFeign.ordersPages(baseOrderParam));
        return FeginResponseTools.returnTipsResponse(orderListTips);
    }

    @GetMapping("/orders/{orderCode}/detail")
    @ApiOperation("根据订单code查询订单详情")
    public ResponseEntity<Tips> orderDetial(@Valid @NotBlank @PathVariable("orderCode") String orderCode, Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        ResponseEntity<Tips> validateResult = validateOrderOwner(userId, orderCode);
        if (Objects.isNull(validateResult) || validateResult.getStatusCode().isError()) {
            return validateResult;
        }
        Tips<OrderDetailResult> orderDetailResultTips = FeginResponseTools.convertResponse(orderServiceFeign.orderDetail(orderCode, true, true));
        return FeginResponseTools.returnTipsResponse(orderDetailResultTips);
    }

    @GetMapping("/orders/count/status")
    @ApiOperation("统计我的用户订单各状态数量")
    public ResponseEntity<Tips<Map<String, Integer>>> countStatus(Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());

        Map<String, Integer> result = new HashMap<>(3);
        ResponseEntity<Tuple<OrderDetailResult>> waitPayment = orderServiceFeign.ordersByUserId(userId, OrderType.NORMAL, OrderStatus.WAIT_PAYMENT);
        Tips<Tuple<OrderDetailResult>> waitPaymentTips = FeginResponseTools.convertResponse(waitPayment);
        if (waitPaymentTips.err()) {
            result.put("waitPaymentCount", waitPaymentTips.getData().getArray().size());
        } else {
            result.put("waitPaymentCount", 0);
        }
        ResponseEntity<Tuple<OrderDetailResult>> waitReceive = orderServiceFeign.ordersByUserId(userId, OrderType.NORMAL, OrderStatus.DISPATCHING);//配送中
        Tips<Tuple<OrderDetailResult>> waitReceiveTips = FeginResponseTools.convertResponse(waitReceive);
        if (waitReceiveTips.err()) {
            result.put("waitReceiveCount", waitReceiveTips.getData().getArray().size());
        } else {
            result.put("waitReceiveCount", 0);
        }
        ResponseEntity<Tuple<OrderDetailResult>> returning = orderServiceFeign.ordersByUserId(userId, OrderType.NORMAL, OrderStatus.RETURNING);
        Tips<Tuple<OrderDetailResult>> returningTips = FeginResponseTools.convertResponse(returning);
        if (returningTips.err()) {
            result.put("returningCount", returningTips.getData().getArray().size());
        } else {
            result.put("returningCount", 0);
        }
        Tips<Map<String, Integer>> tips = new Tips<>();
        tips.data(result);
        return ResponseEntity.ok(tips);
    }

    @PostMapping("/orders/{orderCode}/payment-sign")
    @ApiOperation("订单微信支付签名")
    public ResponseEntity<Tips> paymentSign(@Valid @NotBlank @PathVariable("orderCode") String orderCode, HttpServletRequest request, Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        String openId = user.getUser().get("openId").toString();
        ResponseEntity<Tips> validateResult = validateOrderOwner(userId, orderCode);
        if (Objects.isNull(validateResult) || validateResult.getStatusCode().isError()) {
            return validateResult;
        }
        OrderDetailResult orderDetailResult = (OrderDetailResult) validateResult.getBody().getData();
        PaySign paySign = new PaySign();
        paySign.setApplicationType(ApplicationType.HEALTH_GOOD);
        paySign.setBackUrl(wechatPayConfig.getOrderCallbackUrl());
        paySign.setClientIp(RealClientIp.getRealIp(request));//获取客户端真实ip
        paySign.setConfigName(wechatPayConfig.getConfigName());//微信支付简称
        paySign.setFee(orderDetailResult.getAmountPayable() + orderDetailResult.getDeliveryAmount());
        paySign.setMemo("普通订单支付");
        paySign.setOpenid(openId);
        paySign.setSourceType(SourceType.ORDER);
        paySign.setUserId(userId);
        paySign.setAttach(orderCode);
        Tips<String> wxSignResponse = FeginResponseTools.convertResponse(paymentServiceFeign.wxSign(paySign));
        return FeginResponseTools.returnTipsResponse(wxSignResponse);
    }


    @Sessions.Uncheck
    @PostMapping("/orders/hd-callback")
    @ApiOperation(value = "海鼎回调-后端回调处理")
    public ResponseEntity haidingCallback(HttpServletRequest request) {
        Map<String, Object> parameters = ConvertRequestToMap.convertRequestParameters(request);

        Tips hdCallbackDeal = orderService.hdCallbackDeal(parameters);
        if (hdCallbackDeal.err()) {
            log.info("haidingCallback处理错误:{}", hdCallbackDeal);
        }
        return ResponseEntity.ok(hdCallbackDeal);
    }

    @Sessions.Uncheck
    @PostMapping("/deliver/{deliverType}/callback")
    @ApiOperation(value = "达达配送回调处理订单")
    public ResponseEntity deliverCallback(@PathVariable("deliverType") DeliverType deliverType, HttpServletRequest request) {

        //依据deliverType配送类型处理
        Map<String, Object> parameters = ConvertRequestToMap.convertRequestParameters(request);

        Tips deliverCallbackDeal = orderService.deliverCallbackDeal(parameters);
        if (deliverCallbackDeal.err()) {
            return ResponseEntity.badRequest().body(deliverCallbackDeal);
        }
        return ResponseEntity.ok().build();
    }


    //验证是否属于当前用户的订单
    private ResponseEntity<Tips> validateOrderOwner(Long userId, String orderCode) {

        Tips<OrderDetailResult> orderDetailResultTips = FeginResponseTools.convertResponse(orderServiceFeign.orderDetail(orderCode, false, false));
        if (orderDetailResultTips.err()) {
            return ResponseEntity.badRequest().body(orderDetailResultTips);
        }
        if (!Objects.equals(orderDetailResultTips.getData().getUserId(), userId)) {
            return ResponseEntity.badRequest().body(Tips.warn("当前操作订单不属于登录用户"));
        }
        return ResponseEntity.ok(orderDetailResultTips);
    }


}
