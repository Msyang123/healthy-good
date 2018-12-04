package com.lhiot.healthygood.api.balance;

import com.leon.microx.util.IOUtils;
import com.leon.microx.util.Jackson;
import com.leon.microx.util.StringUtils;
import com.leon.microx.web.result.Tips;
import com.leon.microx.web.session.Sessions;
import com.lhiot.healthygood.feign.OrderServiceFeign;
import com.lhiot.healthygood.feign.PaymentServiceFeign;
import com.lhiot.healthygood.feign.model.OrderDetailResult;
import com.lhiot.healthygood.feign.type.OrderStatus;
import com.lhiot.healthygood.util.FeginResponseTools;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

@Api(description = "鲜果币接口")
@Slf4j
@RestController
public class BalanceApi {
    private final PaymentServiceFeign paymentServiceFeign;
    private final OrderServiceFeign orderServiceFeign;

    @Autowired
    public BalanceApi(PaymentServiceFeign paymentServiceFeign, OrderServiceFeign orderServiceFeign) {
        this.paymentServiceFeign = paymentServiceFeign;
        this.orderServiceFeign = orderServiceFeign;
    }

    @PostMapping("/recharge/payment-sign")
    @ApiOperation("充值签名")
    public ResponseEntity<Tips> paymentSign(@RequestParam("fee") Integer fee, Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        //TODO balanceService设置签名信息
        paymentServiceFeign.paymentSign();//TODO 基础服务未完善
        return ResponseEntity.ok().build();
    }

    @Sessions.Uncheck
    @PostMapping("/recharge/wx-pay/payment-callback")
    @ApiOperation("充值支付微信回调-后端回调处理")
    public ResponseEntity<String> wxPayPaymentCallback(HttpServletRequest request) {
        Map<String, Object> parameters = this.convertRequestParameters(request);
        //调用基础服务验证参数签名是否正确
        paymentServiceFeign.paymentSign();//TODO 基础服务未完善
        //修改充值状态
        orderServiceFeign.updateOrderStatus("", OrderStatus.WAIT_SEND_OUT);//TODO 需要基础服务提供
        return ResponseEntity.ok("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<xml><return_code><![CDATA[SUCCESS]]></return_code>"
                + "<return_msg><![CDATA[OK]]></return_msg></xml>");
    }

    @Sessions.Uncheck
    @PostMapping("/recharge/ali-pay/payment-callback")
    @ApiOperation("充值支付支付宝回调-后端回调处理")
    public ResponseEntity<String> aliPayPaymentCallback(HttpServletRequest request) {
        Map<String, Object> parameters = this.convertRequestParameters(request);
        //调用基础服务验证参数签名是否正确
        paymentServiceFeign.paymentSign();//TODO 基础服务未完善
        //修改充值状态
        return ResponseEntity.ok("success");
    }

    @PostMapping("/balance/payment")
    @ApiOperation(value = "鲜果币支付接口")
    public ResponseEntity balancePayment(@RequestParam("fee") Integer fee, @RequestParam("orderCode") String orderCode, Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        ResponseEntity<Tips> validateResult = validateOrderOwner(userId, orderCode);
        if (Objects.isNull(validateResult) || validateResult.getStatusCode().isError()) {
            return validateResult;
        }
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

    /**
     * 将request中流转换成Map参数
     *
     * @param request
     * @return
     */
    @Nullable
    private Map<String, Object> convertRequestParameters(HttpServletRequest request) {
        Map<String, Object> parameters = null;
        try (InputStream inputStream = request.getInputStream()) {
            if (Objects.nonNull(inputStream)) {
                @Cleanup BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                String parameterString = StringUtils.collectionToDelimitedString(IOUtils.readLines(in), "");
                log.info("request转换成字符串结果：{}", parameterString);
                if (StringUtils.isNotBlank(parameterString)) {
                    parameters = Jackson.map(parameterString);
                }
            }
        } catch (IOException ignore) {
            log.error("convertRequestParameters", ignore);
        }
        return parameters;
    }

}
