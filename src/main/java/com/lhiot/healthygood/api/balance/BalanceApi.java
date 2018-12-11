package com.lhiot.healthygood.api.balance;

import com.leon.microx.web.session.Sessions;
import com.lhiot.healthygood.config.HealthyGoodConfig;
import com.lhiot.healthygood.feign.OrderServiceFeign;
import com.lhiot.healthygood.feign.PaymentServiceFeign;
import com.lhiot.healthygood.feign.model.OrderDetailResult;
import com.lhiot.healthygood.feign.model.PaySign;
import com.lhiot.healthygood.feign.type.ApplicationType;
import com.lhiot.healthygood.feign.type.SourceType;
import com.lhiot.healthygood.util.RealClientIp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@Api(description = "鲜果币接口")
@Slf4j
@RestController
public class BalanceApi {
    private final PaymentServiceFeign paymentServiceFeign;
    private final OrderServiceFeign orderServiceFeign;
    private final HealthyGoodConfig.WechatPayConfig wechatPayConfig;

    @Autowired
    public BalanceApi(PaymentServiceFeign paymentServiceFeign, OrderServiceFeign orderServiceFeign, HealthyGoodConfig healthyGoodConfig) {
        this.paymentServiceFeign = paymentServiceFeign;
        this.orderServiceFeign = orderServiceFeign;
        this.wechatPayConfig = healthyGoodConfig.getWechatPay();
    }

    @PostMapping("/recharge/payment-sign")
    @ApiOperation("充值签名")
    public ResponseEntity<String> paymentSign(@RequestParam("fee") Integer fee, HttpServletRequest request, Sessions.User user) {
        String openId = user.getUser().get("openId").toString();
        Long userId = Long.valueOf(user.getUser().get("userId").toString());

        PaySign paySign = new PaySign();
        paySign.setApplicationType(ApplicationType.HEALTH_GOOD);
        paySign.setBackUrl(wechatPayConfig.getRechargeCallbackUrl());
        paySign.setClientIp(RealClientIp.getRealIp(request));//获取客户端真实ip
        paySign.setConfigName(wechatPayConfig.getConfigName());//微信支付简称
        paySign.setFee(fee);
        paySign.setMemo("充值支付");
        paySign.setOpenid(openId);
        paySign.setSourceType(SourceType.RECHARGE);
        paySign.setUserId(userId);
        paySign.setAttach(user.getUser().get("userId").toString());
        return paymentServiceFeign.wxSign(paySign);
    }


    @PostMapping("/balance/payment")
    @ApiOperation(value = "鲜果币支付接口")
    public ResponseEntity balancePayment(@RequestParam("orderCode") String orderCode, Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        ResponseEntity validateResult = validateOrderOwner(userId, orderCode);
        if (Objects.isNull(validateResult) || validateResult.getStatusCode().isError()) {
            return validateResult;
        }
        OrderDetailResult orderDetailResult = (OrderDetailResult) validateResult.getBody();
        //TODO paymentServiceFeign.  调用鲜果币支付接口
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

}
