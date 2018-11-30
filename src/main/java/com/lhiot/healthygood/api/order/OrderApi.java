package com.lhiot.healthygood.api.order;

import com.leon.microx.web.session.Sessions;
import com.lhiot.healthygood.domain.good.ProductShelf;
import com.lhiot.healthygood.domain.order.CreateOrderParam;
import com.lhiot.healthygood.domain.order.OrderProductParam;
import com.lhiot.healthygood.domain.store.Store;
import com.lhiot.healthygood.type.ReceivingWay;
import com.lhiot.healthygood.feign.BaseDataServiceFeign;
import com.lhiot.healthygood.service.order.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Api(description = "普通订单接口")
@Slf4j
@RestController
public class OrderApi {
    private final OrderService orderService;
    private final BaseDataServiceFeign baseDataServiceFeign;
    private Sessions session;
    @Autowired
    public OrderApi(ObjectProvider<Sessions> sessionsObjectProvider,
                    OrderService orderService,
                    BaseDataServiceFeign  baseDataServiceFeign ) {
        this.orderService = orderService;
        this.session = sessionsObjectProvider.getIfAvailable();
        this.baseDataServiceFeign = baseDataServiceFeign;
    }

    @PostMapping("/orders")
    @ApiOperation("和色果膳--创建订单")
    @ApiImplicitParam(paramType = "body", name = "orderParam", dataType = "CreateOrderParam", required = true, value = "创建订单传入参数")
    public ResponseEntity createOrder(HttpServletRequest request, @RequestBody CreateOrderParam orderParam) throws Exception {
        Integer couponAmount = orderParam.getCouponAmount();
        Integer payableAmount = orderParam.getAmountPayable();
        //验证参数中优惠金额及商品
        boolean flag = orderService.validationParam(orderParam);
        if (!flag) {
            return ResponseEntity.badRequest().body("订单金额或商品异常");
        }
        //送货上门的订单，地址不能为空
        ReceivingWay receivingWay = orderParam.getReceivingWay();
        if (ReceivingWay.TO_THE_HOME.equals(receivingWay)) {
            if (Objects.isNull(orderParam.getAddress())) {
                return ResponseEntity.badRequest().body("送货上门，地址为空");
            }
        }
        //获取用户信息
        String sessionId = session.id(request);
//        Long userId = Long.valueOf(session.user(sessionId).getUser().get("userId").toString());
        Map<String, Object> user = session.user(sessionId).getUser();
        if (Objects.isNull(user)) {
            return ResponseEntity.badRequest().body("用户不存在");
        }
        //判断门店是否存在
        Store store = baseDataServiceFeign.findStoreById(orderParam.getStoreId()).getBody();
        if (Objects.isNull(store)) {
            return ResponseEntity.badRequest().body("门店不存在");
        }
        String storeCode = store.getCode();
        //套餐id构建成以逗号分割的字符串
        List<String> standardIds = orderParam.getOrderProducts().parallelStream()
                .map(OrderProductParam::getStandardId)
                .map(String::valueOf).collect(Collectors.toList());
        for(String standardId:standardIds){
            ProductShelf productShelf = baseDataServiceFeign.singleShelf(Long.valueOf(standardId)).getBody();
            if(null != productShelf){
                return ResponseEntity.badRequest().body("获取商品失败");
            }
        }
//        //到hd验证库存
//        boolean skuFlag = wxsmallShopService.checkHdSku(storeCode, productsStandardResults);
//        if (!skuFlag) {
//            //return ResponseEntity.badRequest().body("订单商品库存不足");
//        }
//        BaseOrderInfo baseOrderInfo = wxsmallShopService.createOrder(orderParam, productsStandardResults, storeCode);
//        try {
//            if(null != wxsmallShopActivityBaseResult){
//                List<WxsmallShopActivityProduct> activityList  = wxsmallShopActivityBaseResult.getDatas();
//                for(WxsmallShopActivityProduct wxsmallShopActivityProduct:activityList){
//                    wxsmallShopActivityProduct.setOrderId(baseOrderInfo.getId());
//                }
//                wxsmallShopActivityFeign.record(activityList,orderParam.getUserId());
//            }
//        }catch (Exception e){
//            log.info("订单数据同步活动模块失败,订单id="+baseOrderInfo.getId());
//        }
//
//        //mq设置三十分钟失效
//        rabbit.convertAndSend(DelayExchange.ORDER_DELAY.getExchangeName(), DelayExchange.DelayQueues.WXSMALL_SHOP_ORDER_TIMEOUT.getDelayName(),
//                Jackson.json(baseOrderInfo), message -> {
//                    message.getMessageProperties().setExpiration(String.valueOf(30 * 60 * 1000));
//                    return message;
//                });
        return ResponseEntity.ok(null);

    }


}
