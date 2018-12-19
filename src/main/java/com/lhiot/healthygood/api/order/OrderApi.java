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
import com.lhiot.healthygood.domain.order.OrderGroupCount;
import com.lhiot.healthygood.domain.user.DoctorCustomer;
import com.lhiot.healthygood.domain.user.FruitDoctor;
import com.lhiot.healthygood.feign.BaseDataServiceFeign;
import com.lhiot.healthygood.feign.OrderServiceFeign;
import com.lhiot.healthygood.feign.PaymentServiceFeign;
import com.lhiot.healthygood.feign.ThirdpartyServerFeign;
import com.lhiot.healthygood.feign.model.*;
import com.lhiot.healthygood.feign.type.*;
import com.lhiot.healthygood.service.order.OrderService;
import com.lhiot.healthygood.service.user.DoctorCustomerService;
import com.lhiot.healthygood.service.user.FruitDoctorService;
import com.lhiot.healthygood.type.ShelfType;
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
    private final DoctorCustomerService doctorCustomerService;
    private final FruitDoctorService fruitDoctorService;
    private final HealthyGoodConfig.WechatPayConfig wechatPayConfig;

    @Autowired
    public OrderApi(BaseDataServiceFeign baseDataServiceFeign,
                    ThirdpartyServerFeign thirdpartyServerFeign,
                    OrderServiceFeign orderServiceFeign,
                    PaymentServiceFeign paymentServiceFeign,
                    OrderService orderService,
                    DoctorCustomerService doctorCustomerService, FruitDoctorService fruitDoctorService, HealthyGoodConfig healthyGoodConfig) {
        this.baseDataServiceFeign = baseDataServiceFeign;
        this.thirdpartyServerFeign = thirdpartyServerFeign;
        this.orderServiceFeign = orderServiceFeign;
        this.paymentServiceFeign = paymentServiceFeign;
        this.orderService = orderService;
        this.doctorCustomerService = doctorCustomerService;
        this.fruitDoctorService = fruitDoctorService;
        this.wechatPayConfig = healthyGoodConfig.getWechatPay();
    }

    @PostMapping("/orders")
    @ApiOperation(value = "和色果膳--创建订单", response = OrderDetailResult.class)
    @ApiImplicitParam(paramType = "body", name = "orderParam", dataType = "CreateOrderParam", required = true, value = "创建订单传入参数")
    public ResponseEntity createOrder(@Valid @RequestBody CreateOrderParam orderParam, Sessions.User user) {

        Map<String, Object> sessionUserMap = user.getUser();
        Long userId = Long.valueOf(sessionUserMap.get("userId").toString());
        orderParam.setUserId(userId);//设置业务用户id
        orderParam.setApplicationType(ApplicationType.HEALTH_GOOD);
        orderParam.setOrderType(OrderType.NORMAL);//普通订单

        String storeCode = orderParam.getOrderStore().getStoreCode();
        //判断门店是否存在
        ResponseEntity<Store> storeResponseEntity = baseDataServiceFeign.findStoreByCode(storeCode, ApplicationType.HEALTH_GOOD);
        if (Objects.isNull(storeResponseEntity) || storeResponseEntity.getStatusCode().isError()) {
            return storeResponseEntity;
        }
        Store store = storeResponseEntity.getBody();

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
        productShelfParam.setIncludeProduct(true);
        productShelfParam.setShelfType(ShelfType.NORMAL);
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
        ResponseEntity<Map<String, Object>> quserSkuTips = thirdpartyServerFeign.querySku(orderParam.getOrderStore().getStoreCode(), barcodes);

        if (Objects.isNull(quserSkuTips) || quserSkuTips.getStatusCode().isError()) {
            return quserSkuTips;
        }
        List<Map> businvs = (List<Map>) quserSkuTips.getBody().get("businvs");
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
        if (Objects.isNull(orderDetailResultResponse)) {
            return ResponseEntity.badRequest().body("创建订单错误");
        } else if (orderDetailResultResponse.getStatusCode().isError()) {
            return orderDetailResultResponse;
        }
        //TODO mq设置三十分钟失效
        return orderDetailResultResponse;
    }

    @DeleteMapping("/orders/{orderCode}")
    @ApiOperation("和色果膳--取消订单")
    @ApiImplicitParam(paramType = "path", name = "orderCode", dataType = "String", required = true, value = "订单id")
    public ResponseEntity createOrder(@Valid @NotBlank @PathVariable("orderCode") String orderCode, Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        ResponseEntity validateResult = validateOrderOwner(userId, orderCode);
        if (Objects.isNull(validateResult) || validateResult.getStatusCode().isError()) {
            return validateResult;
        }
        return orderServiceFeign.updateOrderStatus(orderCode, OrderStatus.FAILURE);
    }


    @PutMapping("/orders/{orderCode}/refund")
    @ApiOperation(value = "订单退货", response = String.class)
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "orderCode", dataType = "String", required = true, value = "订单编码"),
            @ApiImplicitParam(paramType = "body", name = "returnOrderParam", dataType = "ReturnOrderParam", required = true, value = "退货参数")
    })
    public ResponseEntity refundOrder(@Valid @NotBlank @PathVariable("orderCode") String orderCode,
                                      @NotNull @RequestBody ReturnOrderParam returnOrderParam,
                                      Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        ResponseEntity validateResult = validateOrderOwner(userId, orderCode);
        if (Objects.isNull(validateResult) || validateResult.getStatusCode().isError()) {
            return validateResult;
        }
        OrderDetailResult orderDetailResult = (OrderDetailResult) validateResult.getBody();
        ResponseEntity refundOrderTips = null;
        switch (orderDetailResult.getStatus()) {
            //订单未发送海鼎，退款
            case WAIT_SEND_OUT:
                refundOrderTips = orderServiceFeign.notSendHdRefundOrder(orderCode, returnOrderParam);
                break;
            //海鼎备货后提交退货
            case WAIT_DISPATCHING:
                refundOrderTips = orderServiceFeign.returnsRefundOrder(orderCode, returnOrderParam);
                break;
            //已收货
            case RECEIVED:
                refundOrderTips = orderServiceFeign.returnsRefundOrder(orderCode, returnOrderParam);
                break;
            //订单发送海鼎，未备货退货
            case SEND_OUTING:
                refundOrderTips = orderServiceFeign.sendHdRefundOrder(orderCode, returnOrderParam);
                break;

        }

        return refundOrderTips;
    }

    //111

    @PostMapping("/orders/status")
    @ApiOperation(value = "我的用户订单列表")
    public ResponseEntity<Pages<OrderDetailResult>> orderPages(@RequestBody BaseOrderParam baseOrderParam,
                                                               Sessions.User user) {
        baseOrderParam.setUserIds(user.getUser().get("userId").toString());
        baseOrderParam.setOrderType(OrderType.NORMAL);
        return orderServiceFeign.ordersPages(baseOrderParam);
    }

    @PostMapping("/orders/fruit-doctor/customers")
    @ApiOperation(value = "我的鲜果师客户订单列表", response = OrderDetailResult.class, responseContainer = "List")
    public ResponseEntity customersOrders(@RequestBody BaseOrderParam baseOrderParam, Sessions.User user) {
        String userId = user.getUser().get("userId").toString();
        FruitDoctor fruitDoctor = fruitDoctorService.selectByUserId(Long.valueOf(userId));
        if (Objects.isNull(fruitDoctor)) {
            return ResponseEntity.badRequest().body("鲜果师不存在");
        }
        //鲜果师客户列表
        List<DoctorCustomer> doctorCustomerList = doctorCustomerService.selectByDoctorId(fruitDoctor.getId());
        String userIds = StringUtils.arrayToCommaDelimitedString(doctorCustomerList.parallelStream().map(DoctorCustomer::getUserId).toArray(Object[]::new));
        baseOrderParam.setUserIds(userIds);
        baseOrderParam.setOrderType(OrderType.NORMAL);
        return orderServiceFeign.ordersPages(baseOrderParam);
    }

    @GetMapping("/orders/{orderCode}/detail")
    @ApiOperation(value = "根据订单code查询订单详情", response = OrderDetailResult.class)
    public ResponseEntity orderDetial(@Valid @NotBlank @PathVariable("orderCode") String orderCode, Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        ResponseEntity validateResult = validateOrderOwner(userId, orderCode);
        if (Objects.isNull(validateResult) || validateResult.getStatusCode().isError()) {
            return validateResult;
        }
        return orderServiceFeign.orderDetail(orderCode, true, true);
    }

    @GetMapping("/orders/count/status")
    @ApiOperation("统计我的用户订单各状态数量")
    public ResponseEntity<OrderGroupCount> countStatus(Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());

        OrderGroupCount orderGroupCount = new OrderGroupCount();
        ResponseEntity<Tuple<OrderDetailResult>> waitPayment = orderServiceFeign.ordersByUserId(userId, OrderType.NORMAL, OrderStatus.WAIT_PAYMENT);
        Tips<Tuple<OrderDetailResult>> waitPaymentTips = FeginResponseTools.convertResponse(waitPayment);
        if (waitPaymentTips.err()) {
            orderGroupCount.setWaitPaymentCount(waitPaymentTips.getData().getArray().size());
        } else {
            orderGroupCount.setWaitPaymentCount(0);
        }
        ResponseEntity<Tuple<OrderDetailResult>> waitReceive = orderServiceFeign.ordersByUserId(userId, OrderType.NORMAL, OrderStatus.DISPATCHING);//配送中
        Tips<Tuple<OrderDetailResult>> waitReceiveTips = FeginResponseTools.convertResponse(waitReceive);
        if (waitReceiveTips.err()) {
            orderGroupCount.setWaitReceiveCount(waitReceiveTips.getData().getArray().size());
        } else {
            orderGroupCount.setWaitReceiveCount(0);
        }
        ResponseEntity<Tuple<OrderDetailResult>> returning = orderServiceFeign.ordersByUserId(userId, OrderType.NORMAL, OrderStatus.RETURNING);
        Tips<Tuple<OrderDetailResult>> returningTips = FeginResponseTools.convertResponse(returning);
        if (returningTips.err()) {
            orderGroupCount.setReturningCount(returningTips.getData().getArray().size());
        } else {
            orderGroupCount.setReturningCount(0);
        }
        return ResponseEntity.ok(orderGroupCount);
    }

    @PostMapping("/orders/{orderCode}/payment-sign")
    @ApiOperation(value = "订单微信支付签名", response = String.class)
    public ResponseEntity paymentSign(@Valid @NotBlank @PathVariable("orderCode") String orderCode, HttpServletRequest request, Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        String openId = user.getUser().get("openId").toString();
        ResponseEntity validateResult = validateOrderOwner(userId, orderCode);
        if (Objects.isNull(validateResult) || validateResult.getStatusCode().isError()) {
            return validateResult;
        }
        OrderDetailResult orderDetailResult = (OrderDetailResult) validateResult.getBody();
        WxPayModel wxPayModel = new WxPayModel();
        wxPayModel.setApplicationType(ApplicationType.HEALTH_GOOD);
        wxPayModel.setBackUrl(wechatPayConfig.getOrderCallbackUrl());
        wxPayModel.setClientIp(RealClientIp.getRealIp(request));//获取客户端真实ip
        wxPayModel.setConfigName(wechatPayConfig.getConfigName());//微信支付简称
        wxPayModel.setFee(orderDetailResult.getAmountPayable() + orderDetailResult.getDeliveryAmount());
        wxPayModel.setMemo("普通订单支付");
        wxPayModel.setOpenid(openId);
        wxPayModel.setSourceType(SourceType.ORDER);
        wxPayModel.setUserId(userId);
        wxPayModel.setAttach(orderCode);
        return paymentServiceFeign.wxJsSign(wxPayModel);
    }


    @Sessions.Uncheck
    @PostMapping("/orders/hd-callback")
    @ApiOperation(value = "海鼎回调-后端回调处理")
    public ResponseEntity haidingCallback(HttpServletRequest request) {
        Map<String, Object> parameters = ConvertRequestToMap.convertRequestParameters(request);

        Tips hdCallbackDeal = orderService.hdCallbackDeal(parameters);
        if (hdCallbackDeal.err()) {
            log.info("haidingCallback处理错误:{}", hdCallbackDeal);
            return ResponseEntity.badRequest().body(hdCallbackDeal.getMessage());
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
    private ResponseEntity validateOrderOwner(Long userId, String orderCode) {

        ResponseEntity<OrderDetailResult> orderDetailResultTips = orderServiceFeign.orderDetail(orderCode, false, false);
        if (Objects.isNull(orderDetailResultTips) || orderDetailResultTips.getStatusCode().isError()) {
            return orderDetailResultTips;
        }
        if (!Objects.equals(orderDetailResultTips.getBody().getUserId(), userId)) {
            return ResponseEntity.badRequest().body("当前操作订单不属于登录用户");
        }
        return ResponseEntity.ok(orderDetailResultTips);
    }


}
