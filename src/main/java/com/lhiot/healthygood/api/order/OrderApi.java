package com.lhiot.healthygood.api.order;

import com.leon.microx.predefine.OnOff;
import com.leon.microx.util.Beans;
import com.leon.microx.util.Calculator;
import com.leon.microx.util.StringUtils;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.leon.microx.web.session.Sessions;
import com.lhiot.healthygood.config.HealthyGoodConfig;
import com.lhiot.healthygood.domain.activity.ActivityProduct;
import com.lhiot.healthygood.domain.activity.ActivityProductRecord;
import com.lhiot.healthygood.domain.activity.SpecialProductActivity;
import com.lhiot.healthygood.domain.doctor.DoctorAchievementLog;
import com.lhiot.healthygood.domain.order.OrderGroupCount;
import com.lhiot.healthygood.domain.user.DoctorCustomer;
import com.lhiot.healthygood.domain.user.FruitDoctor;
import com.lhiot.healthygood.feign.BaseDataServiceFeign;
import com.lhiot.healthygood.feign.OrderServiceFeign;
import com.lhiot.healthygood.feign.PaymentServiceFeign;
import com.lhiot.healthygood.feign.ThirdpartyServerFeign;
import com.lhiot.healthygood.feign.model.*;
import com.lhiot.healthygood.feign.type.*;
import com.lhiot.healthygood.mq.HealthyGoodQueue;
import com.lhiot.healthygood.service.activity.ActivityProductRecordService;
import com.lhiot.healthygood.service.activity.ActivityProductService;
import com.lhiot.healthygood.service.activity.SpecialProductActivityService;
import com.lhiot.healthygood.service.customplan.CustomOrderService;
import com.lhiot.healthygood.service.doctor.DoctorAchievementLogService;
import com.lhiot.healthygood.service.order.OrderService;
import com.lhiot.healthygood.service.user.DoctorCustomerService;
import com.lhiot.healthygood.service.user.FruitDoctorService;
import com.lhiot.healthygood.type.ActivityType;
import com.lhiot.healthygood.type.CustomOrderDeliveryStatus;
import com.lhiot.healthygood.type.ShelfType;
import com.lhiot.healthygood.util.ConvertRequestToMap;
import com.lhiot.healthygood.util.FeginResponseTools;
import com.lhiot.healthygood.util.RealClientIp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Api(description = "普通订单接口")
@Slf4j
@RestController
public class OrderApi {
    private final BaseDataServiceFeign baseDataServiceFeign;
    private final ThirdpartyServerFeign thirdpartyServerFeign;
    private final OrderServiceFeign orderServiceFeign;
    private final PaymentServiceFeign paymentServiceFeign;
    private final OrderService orderService;
    private final CustomOrderService customOrderService;
    private final DoctorCustomerService doctorCustomerService;
    private final FruitDoctorService fruitDoctorService;
    private final HealthyGoodConfig.WechatPayConfig wechatPayConfig;
    private final ActivityProductService activityProductService;
    private final ActivityProductRecordService activityProductRecordService;
    private final SpecialProductActivityService specialProductActivityService;
    private final RabbitTemplate rabbitTemplate;
    private final DoctorAchievementLogService doctorAchievementLogService;

    @Autowired
    public OrderApi(BaseDataServiceFeign baseDataServiceFeign,
                    ThirdpartyServerFeign thirdpartyServerFeign,
                    OrderServiceFeign orderServiceFeign,
                    PaymentServiceFeign paymentServiceFeign,
                    OrderService orderService,
                    CustomOrderService customOrderService, DoctorCustomerService doctorCustomerService, FruitDoctorService fruitDoctorService, HealthyGoodConfig healthyGoodConfig, RabbitTemplate rabbitTemplate, ActivityProductService activityProductService, ActivityProductRecordService activityProductRecordService, SpecialProductActivityService specialProductActivityService, DoctorAchievementLogService doctorAchievementLogService) {
        this.baseDataServiceFeign = baseDataServiceFeign;
        this.thirdpartyServerFeign = thirdpartyServerFeign;
        this.orderServiceFeign = orderServiceFeign;
        this.paymentServiceFeign = paymentServiceFeign;
        this.orderService = orderService;
        this.customOrderService = customOrderService;
        this.doctorCustomerService = doctorCustomerService;
        this.fruitDoctorService = fruitDoctorService;
        this.wechatPayConfig = healthyGoodConfig.getWechatPay();
        this.rabbitTemplate = rabbitTemplate;
        this.activityProductService = activityProductService;
        this.activityProductRecordService = activityProductRecordService;
        this.specialProductActivityService = specialProductActivityService;
        this.doctorAchievementLogService = doctorAchievementLogService;
    }

    @PostMapping("/orders")
    @ApiOperation(value = "和色果膳--创建订单*", response = OrderDetailResult.class)
    @ApiImplicitParam(paramType = "body", name = "orderParam", dataType = "CreateOrderParam", required = true, value = "创建订单传入参数")
    public ResponseEntity createOrder(@Valid @RequestBody CreateOrderParam orderParam, Sessions.User user) {

        Map<String, Object> sessionUserMap = user.getUser();
        Long userId = Long.valueOf(sessionUserMap.get("userId").toString());
        orderParam.setUserId(userId);//设置业务用户id
        orderParam.setApplicationType(ApplicationType.HEALTH_GOOD);
        orderParam.setOrderType(OrderType.NORMAL);//普通订单
        orderParam.setAllowRefund(AllowRefund.YES);

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
            return ResponseEntity.badRequest().body(productShelfTips.getMessage());
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
                            String.format("%s实际库存%s低于安全库存%d，不允许销售", productShelf.getProductSpecification().getBarcode(), businv.get("qty").toString(), productShelf.getProductSpecification().getLimitInventory())
                    );
                }
            }
        }

        ActivityProduct productParam = new ActivityProduct();
        productParam.setProductShelfIds(StringUtils.arrayToCommaDelimitedString(shelfIds));
        List<ActivityProduct> activityProducts = activityProductService.list(productParam);
        Map<Long, ActivityProduct> activitPriceMap = activityProducts.stream().map(item -> {
            ActivityProductRecord recordParam = new ActivityProductRecord();
            recordParam.setUserId(userId);
            recordParam.setProductShelfId(item.getProductShelfId());
            Integer counts = activityProductRecordService.selectRecordCount(recordParam);
            item.setAlreadyBuyCount(counts);
            SpecialProductActivity specialProductActivity = specialProductActivityService.selectActivity();
            item.setLimitCount(specialProductActivity.getLimitCount());
            return item;
        }).collect(Collectors.toMap(ActivityProduct::getProductShelfId, obj -> obj));//把活动商品id和活动价格封装成Map
        AtomicReference<Integer> productAmount = new AtomicReference<>(0);
        AtomicReference<Integer> disCountPrice = new AtomicReference<>(0);

        //给商品赋值规格数量
        orderParam.getOrderProducts().forEach(orderProduct -> productShelfPages.getArray().stream()
                //上架id相同的订单商品信息，通过基础服务获取的赋值给订单商品信息
                .filter(productShelf -> Objects.equals(orderProduct.getShelfId(), productShelf.getId()))
                .forEach(item -> {
                    //BeanUtils.of(orderProduct).ignoreField("id").populate(item);//忽略掉id 对象赋值
                    Beans.wrap(orderProduct).copyOf(item);
                    //设置规格信息
                    ProductSpecification productSpecification = item.getProductSpecification();
                    orderProduct.setBarcode(productSpecification.getBarcode());
                    BigDecimal weight = new BigDecimal(productSpecification.getWeight().toString());
                    BigDecimal quty = new BigDecimal(orderProduct.getProductQty());
                    orderProduct.setTotalWeight(weight.multiply(quty));
                    orderProduct.setSpecificationId(productSpecification.getId());
                    //如果存在特价就用特价
                    Integer price = Objects.isNull(item.getPrice()) ? item.getOriginalPrice() : item.getPrice();
                    //新品尝鲜的超限价格计算
                    if (activitPriceMap.containsKey(item.getId())) {
                        ActivityProduct ac = activitPriceMap.get(item.getId());
                        Integer canBuyActivityCounts = ac.getLimitCount() - ac.getAlreadyBuyCount();//计算出剩余可购买次数
                        Integer originalCounts = orderProduct.getProductQty() - canBuyActivityCounts;//超过活动限购后，按原价购买数量
                        Integer discountPrice = canBuyActivityCounts > orderProduct.getProductQty() ? (int) Calculator.mul(ac.getActivityPrice(), orderProduct.getProductQty()) : (int) Calculator.add(Calculator.mul(price, originalCounts), Calculator.mul(ac.getActivityPrice(), canBuyActivityCounts));
                        orderProduct.setDiscountPrice(discountPrice);
                    } else {
                        orderProduct.setDiscountPrice((int) Calculator.mul(price, orderProduct.getProductQty()));//去除优惠有单品总价
                    }
                    orderProduct.setTotalPrice((int) Calculator.mul(item.getPrice(), orderProduct.getProductQty()));//单品总价
                    orderProduct.setProductName(item.getName());
                    productAmount.updateAndGet(v -> v + orderProduct.getTotalPrice());
                    disCountPrice.updateAndGet(v -> v + orderProduct.getDiscountPrice());
                })
        );
        orderParam.setTotalAmount(productAmount.get());//订单总价
        orderParam.setAmountPayable(disCountPrice.get());//应付金额
        orderParam.setCouponAmount(productAmount.get() - disCountPrice.get());//订单优惠金额

        ResponseEntity<OrderDetailResult> orderDetailResultResponse = orderServiceFeign.createOrder(orderParam);
        if (Objects.isNull(orderDetailResultResponse)) {
            return ResponseEntity.badRequest().body("创建订单错误");
        } else if (orderDetailResultResponse.getStatusCode().isError()) {
            return orderDetailResultResponse;
        }
        if (activityProducts.size() > 0) {
            activityProducts.forEach(activityProduct -> {
                orderParam.getOrderProducts().stream().filter(orderProduct -> Objects.equals(orderProduct.getShelfId(), activityProduct.getProductShelfId())).forEach(map -> {
                    ActivityProductRecord recordParam = new ActivityProductRecord();
                    recordParam.setUserId(userId);
                    recordParam.setProductShelfId(activityProduct.getProductShelfId());
                    Integer counts = activityProductRecordService.selectRecordCount(recordParam);
                    if (counts <= activityProduct.getLimitCount()) {
                        int canBuyCount = activityProduct.getLimitCount() - counts;//可用数量
                        counts = map.getProductQty() > canBuyCount ? canBuyCount : map.getProductQty();
                        for (int i = 0; counts > i; i++) {
                            ActivityProductRecord record = new ActivityProductRecord();
                            record.setProductShelfId(activityProduct.getProductShelfId());
                            record.setUserId(userId);
                            record.setActivityId(activityProduct.getActivityId());
                            record.setOrderCode(orderDetailResultResponse.getBody().getCode());
                            record.setActivityType(ActivityType.NEW_SPECIAL);
                            activityProductRecordService.create(record);
                        }
                    }

                });

            });
        }
        //mq设置三十分钟未支付失效
        HealthyGoodQueue.DelayQueue.CANCEL_ORDER.send(rabbitTemplate, orderDetailResultResponse.getBody().getCode(), 30 * 60 * 1000);
        return orderDetailResultResponse;
    }

    @DeleteMapping("/orders/{orderCode}")
    @ApiOperation("和色果膳--取消订单*")
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
    @ApiOperation(value = "订单退货*", response = String.class)
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
        ResponseEntity refundOrder = null;
        returnOrderParam.setNotifyUrl(wechatPayConfig.getOrderRefundCallbackUrl());//设置退款回调
        //查询退款金额
        ResponseEntity<Fee> refundFeeResponseEntity = orderServiceFeign.refundFee(orderCode, returnOrderParam.getOrderProductIds(), returnOrderParam.getRefundType());
        Tips<Fee> refundFeeTips = FeginResponseTools.convertResponse(refundFeeResponseEntity);
        AtomicReference<Integer> refundFeeSum = new AtomicReference<>(0);
        //计算订单金额
        if (refundFeeTips.err()) {
            //如果查询失败那么就直接使用本地计算的退款费用退款
            //给商品赋值规格数量
            Arrays.asList(returnOrderParam.getOrderProductIds().split(",")).forEach(refundOrderProductId ->
                    orderDetailResult.getOrderProductList().stream()
                            //上架id相同的订单商品信息，通过基础服务获取的赋值给订单商品信息
                            .filter(orderProduct -> Objects.equals(orderProduct.getId(), Long.valueOf(refundOrderProductId)))
                            .forEach(item -> refundFeeSum.updateAndGet(v -> v + item.getDiscountPrice()))
            );
        } else {
            Fee refundFee = refundFeeTips.getData();
            refundFeeSum.updateAndGet(v -> v + refundFee.getFee());
        }
        returnOrderParam.setFee(refundFeeSum.get());
        switch (orderDetailResult.getStatus()) {
            //订单未发送海鼎，退款
            case WAIT_SEND_OUT:
                //如果是定制订单，需要退剩余次数+1
                if (Objects.equals(OrderType.CUSTOM, orderDetailResult.getOrderType())) {
                    //定制订单 只退用户剩余次数，不退款
                    Tips refundResult = customOrderService.refundCustomOrderDelivery(orderCode, NotPayRefundWay.NOT_SEND_HD);
                    if (refundResult.err()) {
                        return ResponseEntity.badRequest().body(refundResult.getMessage());
                    }
                } else {
                    refundOrder = orderServiceFeign.notSendHdRefundOrder(orderCode, returnOrderParam);
                }
                break;
            //海鼎备货后提交退货
            case WAIT_DISPATCHING:
                refundOrder = orderServiceFeign.returnsRefundOrder(orderCode, returnOrderParam);
                if (Objects.nonNull(refundOrder) && refundOrder.getStatusCode().is2xxSuccessful()) {
                    customOrderService.updateCustomOrderDeliveryStatus(orderCode, CustomOrderDeliveryStatus.RETURNING);//修改为退货中
                }
                break;
            //已收货
            case RECEIVED:
                refundOrder = orderServiceFeign.returnsRefundOrder(orderCode, returnOrderParam);
                if (Objects.nonNull(refundOrder) && refundOrder.getStatusCode().is2xxSuccessful()) {
                    customOrderService.updateCustomOrderDeliveryStatus(orderCode, CustomOrderDeliveryStatus.RETURNING);//修改为退货中
                }
                break;
            //订单发送海鼎，未备货退货
            case SEND_OUTING:
                //如果是定制订单，需要退剩余次数+1
                if (Objects.equals(OrderType.CUSTOM, orderDetailResult.getOrderType())) {
                    //定制订单 只退用户剩余次数，不退款
                    Tips refundResult = customOrderService.refundCustomOrderDelivery(orderCode, NotPayRefundWay.NOT_STOCKING);
                    if (refundResult.err()) {
                        return ResponseEntity.badRequest().body(refundResult.getMessage());
                    }
                } else {
                    refundOrder = orderServiceFeign.sendHdRefundOrder(orderCode, returnOrderParam);
                }
                break;

        }
        fruitDoctorService.calculationCommission(orderDetailResult);
        return refundOrder;
    }


    @PostMapping("/orders/status")
    @ApiOperation(value = "我的用户订单列表*")
    public ResponseEntity<Pages<OrderDetailResult>> orderPages(@RequestBody BaseOrderParam baseOrderParam,
                                                               Sessions.User user) {
        baseOrderParam.setUserIds(user.getUser().get("userId").toString());
        baseOrderParam.setOrderType(OrderType.NORMAL);
        baseOrderParam.setApplicationType(ApplicationType.HEALTH_GOOD);
        return orderServiceFeign.ordersPages(baseOrderParam);
    }

    @PostMapping("/orders/fruit-doctor/customers")
    @ApiOperation(value = "我的鲜果师客户订单列表*", response = OrderDetailResult.class, responseContainer = "List")
    public ResponseEntity customersOrders(@RequestBody BaseOrderParam baseOrderParam, Sessions.User user) {
        String userId = user.getUser().get("userId").toString();
        FruitDoctor fruitDoctor = fruitDoctorService.selectByUserId(Long.valueOf(userId));
        if (Objects.isNull(fruitDoctor)) {
            return ResponseEntity.badRequest().body("鲜果师不存在");
        }
        List<DoctorAchievementLog> doctorAchievementLogs = doctorAchievementLogService.selectOrderCodeByDoctorId(fruitDoctor.getId());
        if (doctorAchievementLogs.size()<=0){
            return ResponseEntity.ok().body(new Pages<OrderDetailResult>());
        }
        List<String> orderCodes = new ArrayList<>();
        doctorAchievementLogs.forEach(doctorAchievementLog -> {
            orderCodes.add(doctorAchievementLog.getOrderId());
        });
        baseOrderParam.setOrderCodeList(orderCodes);
        baseOrderParam.setApplicationType(ApplicationType.HEALTH_GOOD);
        return orderServiceFeign.ordersPages(baseOrderParam);
    }

    @Sessions.Uncheck
    @GetMapping("/orders/{orderCode}/detail")
    @ApiOperation(value = "根据订单code查询订单详情", response = OrderDetailResult.class)
    public ResponseEntity orderDetial(@Valid @NotBlank @PathVariable("orderCode") String orderCode) {
        ResponseEntity<OrderDetailResult> orderDetailResultResponseEntity = orderServiceFeign.orderDetail(orderCode, true, true);
        Tips<OrderDetailResult> orderDetailResultTips = FeginResponseTools.convertResponse(orderDetailResultResponseEntity);
        if (orderDetailResultTips.err()) {
            return orderDetailResultResponseEntity;
        }
        if (StringUtils.isNotBlank(orderDetailResultTips.getData().getPayId())) {
            ResponseEntity<Record> payLog = paymentServiceFeign.one(orderDetailResultTips.getData().getPayId());
            Tips<Record> recordTips = FeginResponseTools.convertResponse(payLog);
            if (recordTips.err()) {
                return orderDetailResultResponseEntity;
            }
            //设置支付类型
            orderDetailResultResponseEntity.getBody().setTradeType(recordTips.getData().getTradeType());
        }
        return orderDetailResultResponseEntity;
    }

    @GetMapping("/orders/count/status")
    @ApiOperation("统计我的用户订单各状态数量*")
    public ResponseEntity<OrderGroupCount> countStatus(Sessions.User user) {
        OrderGroupCount orderGroupCount = new OrderGroupCount();

        BaseOrderParam baseOrderParam = new BaseOrderParam();
        baseOrderParam.setUserIds(user.getUser().get("userId").toString());
        baseOrderParam.setOrderType(OrderType.NORMAL);
        baseOrderParam.setApplicationType(ApplicationType.HEALTH_GOOD);
        baseOrderParam.setOrderStatuses(new OrderStatus[]{OrderStatus.WAIT_PAYMENT});
        ResponseEntity<Pages<OrderDetailResult>> ordersPages = orderServiceFeign.ordersPages(baseOrderParam);
        Tips<Pages<OrderDetailResult>> waitPaymentListTips = FeginResponseTools.convertResponse(ordersPages);

        //ResponseEntity<Tuple<OrderDetailResult>> waitPayment = orderServiceFeign.ordersByUserId(userId, OrderType.NORMAL, OrderStatus.WAIT_PAYMENT);
        //Tips<Tuple<OrderDetailResult>> waitPaymentTips = FeginResponseTools.convertResponse(waitPayment);
        if (waitPaymentListTips.err()) {
            orderGroupCount.setWaitPaymentCount(0);
        } else {
            orderGroupCount.setWaitPaymentCount(waitPaymentListTips.getData().getTotal());
        }
        //ResponseEntity<Tuple<OrderDetailResult>> waitReceive = orderServiceFeign.ordersByUserId(userId, OrderType.NORMAL, OrderStatus.DISPATCHING);//配送中
        //Tips<Tuple<OrderDetailResult>> waitReceiveTips = FeginResponseTools.convertResponse(waitReceive);
        baseOrderParam.setOrderStatuses(new OrderStatus[]{OrderStatus.WAIT_SEND_OUT, OrderStatus.SEND_OUTING, OrderStatus.WAIT_DISPATCHING, OrderStatus.DISPATCHING});
        ordersPages = orderServiceFeign.ordersPages(baseOrderParam);
        Tips<Pages<OrderDetailResult>> waitReceiveListTips = FeginResponseTools.convertResponse(ordersPages);
        if (waitReceiveListTips.err()) {
            orderGroupCount.setWaitReceiveCount(0);
        } else {
            orderGroupCount.setWaitReceiveCount(waitReceiveListTips.getData().getTotal());
        }
        return ResponseEntity.ok(orderGroupCount);
    }

    @PostMapping("/orders/{orderCode}/payment-sign")
    @ApiOperation(value = "订单微信支付签名*", response = String.class)
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
    @PostMapping("/orders/deliver/{deliverType}/callback")
    @ApiOperation(value = "达达配送回调处理订单")
    public ResponseEntity deliverCallback(@PathVariable("deliverType") DeliverType deliverType, HttpServletRequest request) {
        log.info("配送回调处理订单{}",deliverType);
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

        ResponseEntity<OrderDetailResult> orderDetailResultEntity = orderServiceFeign.orderDetail(orderCode, true, false);
        if (Objects.isNull(orderDetailResultEntity) || orderDetailResultEntity.getStatusCode().isError()) {
            return orderDetailResultEntity;
        }
        if (!Objects.equals(orderDetailResultEntity.getBody().getUserId(), userId)) {
            return ResponseEntity.badRequest().body("当前操作订单不属于登录用户");
        }
        return orderDetailResultEntity;
    }


}
