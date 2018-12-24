package com.lhiot.healthygood.api.balance;

import com.leon.microx.util.Jackson;
import com.leon.microx.web.result.Id;
import com.leon.microx.web.session.Sessions;
import com.lhiot.healthygood.config.HealthyGoodConfig;
import com.lhiot.healthygood.domain.customplan.CustomOrder;
import com.lhiot.healthygood.domain.customplan.CustomOrderTime;
import com.lhiot.healthygood.feign.OrderServiceFeign;
import com.lhiot.healthygood.feign.PaymentServiceFeign;
import com.lhiot.healthygood.feign.model.*;
import com.lhiot.healthygood.feign.type.ApplicationType;
import com.lhiot.healthygood.feign.type.PayType;
import com.lhiot.healthygood.feign.type.SourceType;
import com.lhiot.healthygood.service.customplan.CustomOrderService;
import com.lhiot.healthygood.service.order.OrderService;
import com.lhiot.healthygood.service.user.FruitDoctorService;
import com.lhiot.healthygood.type.CustomOrderStatus;
import com.lhiot.healthygood.util.RealClientIp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Api(description = "鲜果币接口")
@Slf4j
@RestController
public class BalanceApi {
    private final PaymentServiceFeign paymentServiceFeign;
    private final OrderServiceFeign orderServiceFeign;
    private final HealthyGoodConfig.WechatPayConfig wechatPayConfig;
    private final CustomOrderService customOrderService;
    private final OrderService orderService;
    private final FruitDoctorService fruitDoctorService;

    @Autowired
    public BalanceApi(PaymentServiceFeign paymentServiceFeign, OrderServiceFeign orderServiceFeign,
                      HealthyGoodConfig healthyGoodConfig, CustomOrderService customOrderService,
                      FruitDoctorService fruitDoctorService, OrderService orderService) {
        this.paymentServiceFeign = paymentServiceFeign;
        this.orderServiceFeign = orderServiceFeign;
        this.wechatPayConfig = healthyGoodConfig.getWechatPay();
        this.customOrderService = customOrderService;
        this.fruitDoctorService = fruitDoctorService;
        this.orderService = orderService;
    }

    @PostMapping("/recharge/payment-sign")
    @ApiOperation("充值签名")
    public ResponseEntity<String> paymentSign(@RequestBody WxPayModel paySign, HttpServletRequest request, Sessions.User user) {
        String openId = user.getUser().get("openId").toString();
        Long userId = Long.valueOf(user.getUser().get("userId").toString());

        paySign.setApplicationType(ApplicationType.HEALTH_GOOD);
        paySign.setBackUrl(wechatPayConfig.getRechargeCallbackUrl());
        paySign.setClientIp(RealClientIp.getRealIp(request));//获取客户端真实ip
        paySign.setConfigName(wechatPayConfig.getConfigName());//微信支付简称
        //paySign.setFee(fee);
        paySign.setMemo("充值支付");
        paySign.setOpenid(openId);
        paySign.setSourceType(SourceType.RECHARGE);
        paySign.setUserId(userId);
        paySign.setAttach(user.getUser().get("userId").toString());
        ResponseEntity<Map> responseEntity = paymentServiceFeign.wxJsSign(paySign);
        if (Objects.isNull(responseEntity) || responseEntity.getStatusCode().isError()) {
            log.error("调用基础服务充值签名错误{}", responseEntity);
            return ResponseEntity.badRequest().body("调用错误");
        }
        return ResponseEntity.ok(Jackson.json(responseEntity.getBody()));
    }


    @PostMapping("/balance/order/payment")
    @ApiOperation(value = "鲜果币支付普通订单接口*")
    public ResponseEntity balanceOrderPayment(@RequestBody BalancePayModel balancePayModel, Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        String orderCode = balancePayModel.getOrderCode();
        ResponseEntity validateResult = validateOrderOwner(userId, orderCode);
        if (Objects.isNull(validateResult) || validateResult.getStatusCode().isError()) {
            return validateResult;
        }
        OrderDetailResult orderDetailResult = (OrderDetailResult) validateResult.getBody();
        //调用鲜果币支付接口
        balancePayModel.setApplicationType(ApplicationType.HEALTH_GOOD);
        balancePayModel.setFee(orderDetailResult.getAmountPayable() + orderDetailResult.getDeliveryAmount());
        balancePayModel.setMemo("普通订单支付");
        balancePayModel.setSourceType(SourceType.ORDER);
        balancePayModel.setUserId(userId);
        ResponseEntity<Id> balancePayResult = paymentServiceFeign.balancePay(balancePayModel);
        if (Objects.isNull(balancePayResult) || balancePayResult.getStatusCode().isError()) {
            log.error("鲜果币支付失败{}", balancePayResult);
            return ResponseEntity.badRequest().body("鲜果币支付失败");
        }
        String outTradeId = String.valueOf(balancePayResult.getBody().getValue());
        Payed payed = new Payed();
        //payed.setBankType("");
        payed.setPayAt(Date.from(Instant.now()));
        payed.setPayId(outTradeId);
        payed.setPayType(PayType.BALANCE);
        //payed.setTradeId("");
        //修改为已支付
        ResponseEntity updateOrderToPayed = orderServiceFeign.updateOrderToPayed(orderCode, payed);
        if (Objects.isNull(updateOrderToPayed) || updateOrderToPayed.getStatusCode().isError()) {
            log.error("修改为已支付失败{}", updateOrderToPayed);
            return ResponseEntity.badRequest().body("修改为已支付失败");
        }
        ResponseEntity orderDetailResultResponseEntity = orderServiceFeign.orderDetail(balancePayModel.getOrderCode(), false, false);
        if (orderDetailResultResponseEntity.getStatusCode().isError()) {
            return ResponseEntity.badRequest().body(orderDetailResultResponseEntity.getBody());
        }
        OrderDetailResult order = (OrderDetailResult) orderDetailResultResponseEntity.getBody();
        fruitDoctorService.calculationCommission(order);//鲜果师业绩提成
        //延迟发送海鼎
        orderService.delaySendToHd(orderCode, Jackson.object(orderDetailResult.getDeliverAt(), DeliverTime.class));
        return ResponseEntity.ok(orderDetailResult);
    }

    @Sessions.Uncheck
    @PostMapping("/test")
    @ApiOperation(value = "测试延迟消息队列")
    public ResponseEntity test(@RequestBody String deliverTime){
        orderService.delaySendToHd("HG9239875965128704", Jackson.object(deliverTime, DeliverTime.class));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/balance/custom-order/payment")
    @ApiOperation(value = "鲜果币支付定制订单接口*")
    public ResponseEntity balanceCustomOrderPayment(@RequestBody BalancePayModel balancePayModel, Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        ResponseEntity validateResult = validateCustomOrderOwner(userId, balancePayModel.getOrderCode());
        if (Objects.isNull(validateResult) || validateResult.getStatusCode().isError()) {
            return validateResult;
        }
        CustomOrder customOrderDetial = (CustomOrder) validateResult.getBody();
        //调用鲜果币支付接口
        balancePayModel.setApplicationType(ApplicationType.HEALTH_GOOD);
        balancePayModel.setFee(customOrderDetial.getPrice());
        balancePayModel.setMemo("定制订单支付");
        balancePayModel.setSourceType(SourceType.CUSTOM_PLAN);
        balancePayModel.setUserId(userId);
        ResponseEntity balancePayResult = paymentServiceFeign.balancePay(balancePayModel);
        if (Objects.isNull(balancePayResult) || balancePayResult.getStatusCode().isError()) {
            log.error("鲜果币支付定制订单失败{}", balancePayResult);
            return ResponseEntity.badRequest().body("鲜果币支付定制订单失败");
        }
        Id id = (Id) balancePayResult.getBody();
        String outTradeId = String.valueOf(id.getValue());

        //修改定制计划订单状态定制中
        String customOrderCode = balancePayModel.getOrderCode();
        CustomOrder customOrder = new CustomOrder();
        customOrder.setCustomOrderCode(customOrderCode);
        customOrder.setStatus(CustomOrderStatus.CUSTOMING);//定制中
        customOrder.setPayId(outTradeId);//第三方支付id
        log.info("定制计划余额支付-后端修改为支付成功 并保持预支付id{}", customOrder);
        customOrderService.updateByCode(customOrder);

        //自动提取订单自动提取
        CustomOrderTime customOrderTime = Jackson.object(customOrderDetial.getDeliveryTime(), CustomOrderTime.class);
        LocalDateTime deliveryDateTime = LocalDate.now().atTime(customOrderTime.getStartTime());
        deliveryDateTime = deliveryDateTime.plusDays(1);
        customOrderService.scheduleDeliveryCustomOrder(deliveryDateTime, customOrderCode);
        return ResponseEntity.ok().build();
    }

    //验证是否属于当前用户的订单
    private ResponseEntity validateOrderOwner(Long userId, String orderCode) {
        ResponseEntity<OrderDetailResult> responseEntity = orderServiceFeign.orderDetail(orderCode, false, false);
        if (Objects.isNull(responseEntity) || responseEntity.getStatusCode().isError()) {
            return responseEntity;
        }
        OrderDetailResult orderDetailResult = responseEntity.getBody();
        if (!Objects.equals(orderDetailResult.getUserId(), userId)) {
            return ResponseEntity.badRequest().body("当前操作订单不属于登录用户");
        }
        return ResponseEntity.ok(orderDetailResult);
    }

    //验证是否属于当前用户的订单
    private ResponseEntity validateCustomOrderOwner(Long userId, String customOrderCode) {

        CustomOrder customOrder = customOrderService.selectByCode(customOrderCode);
        if (Objects.isNull(customOrder)) {
            return ResponseEntity.badRequest().body("未找到定制订单");
        }
        if (!Objects.equals(customOrder.getUserId(), userId)) {
            return ResponseEntity.badRequest().body("当前操作订单不属于登录用户");
        }
        return ResponseEntity.ok(customOrder);
    }

}
