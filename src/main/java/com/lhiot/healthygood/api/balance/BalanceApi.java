package com.lhiot.healthygood.api.balance;

import com.leon.microx.web.result.Tips;
import com.leon.microx.web.session.Sessions;
import com.lhiot.healthygood.config.HealthyGoodConfig;
import com.lhiot.healthygood.feign.OrderServiceFeign;
import com.lhiot.healthygood.feign.PaymentServiceFeign;
import com.lhiot.healthygood.feign.model.OrderDetailResult;
import com.lhiot.healthygood.feign.model.PaySign;
import com.lhiot.healthygood.feign.type.ApplicationType;
import com.lhiot.healthygood.feign.type.SourceType;
import com.lhiot.healthygood.util.FeginResponseTools;
import com.lhiot.healthygood.util.RealClientIp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<Tips> paymentSign(@RequestParam("fee") Integer fee, HttpServletRequest request, Sessions.User user) {
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
        Tips<String> wxSignResponse = FeginResponseTools.convertResponse(paymentServiceFeign.wxSign(paySign));
        if (wxSignResponse.err()) {
            return ResponseEntity.badRequest().body(wxSignResponse);
        }
        return ResponseEntity.ok(wxSignResponse);
    }


    @PostMapping("/balance/payment")
    @ApiOperation(value = "鲜果币支付接口")
    public ResponseEntity balancePayment(@RequestParam("orderCode") String orderCode, Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        ResponseEntity<Tips> validateResult = validateOrderOwner(userId, orderCode);
        if (Objects.isNull(validateResult) || validateResult.getStatusCode().isError()) {
            return validateResult;
        }
        OrderDetailResult orderDetailResult= (OrderDetailResult) validateResult.getBody().getData();
        //TODO paymentServiceFeign.  调用鲜果币支付接口
        return ResponseEntity.ok().build();
    }


    //验证是否属于当前用户的订单
    private ResponseEntity<Tips> validateOrderOwner(Long userId, String orderCode) {

        Tips<OrderDetailResult> orderDetailResultTips = FeginResponseTools.convertResponse(orderServiceFeign.orderDetail(orderCode, false, false));
        if (orderDetailResultTips.err()) {
            return ResponseEntity.badRequest().body(orderDetailResultTips);
        }
        if (!Objects.equals(orderDetailResultTips.getData().getUserId(), userId)) {
            return ResponseEntity.badRequest().body(Tips.of(HttpStatus.BAD_REQUEST, "当前操作订单不属于登录用户"));
        }
        return ResponseEntity.ok(orderDetailResultTips);
    }

}
