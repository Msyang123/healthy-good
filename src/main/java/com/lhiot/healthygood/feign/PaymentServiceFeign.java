package com.lhiot.healthygood.feign;

import com.lhiot.healthygood.feign.model.PaySign;
import com.lhiot.healthygood.feign.model.Payed;
import com.lhiot.healthygood.feign.model.Refund;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;
import java.util.Map;

@Component
@FeignClient("PAYMENT-SERVICE-V1-0")
public interface PaymentServiceFeign {
    /**********支付宝*******************************/
    //支付签名
    @RequestMapping(value = "/ali-pay/sign", method = RequestMethod.POST)
    ResponseEntity<String> aliPaySign(@Valid @RequestBody PaySign sign);

    //支付 - 验签
    @RequestMapping(value = "/ali-pay/{outTradeNo}/verification", method = RequestMethod.POST)
    ResponseEntity aliPayVerify(@PathVariable("outTradeNo") String outTradeNo, @RequestBody Map<String, String> notifiedParameters);

    //支付 - 撤销
    @RequestMapping(value = "/ali-pay/{outTradeNo}", method = RequestMethod.DELETE)
    ResponseEntity aliPayCancel(@PathVariable("outTradeNo") String outTradeNo);

    //支付 - 退款（支持部分、多次退款)
    @RequestMapping(value = "/ali-pay/{outTradeNo}", method = RequestMethod.PUT)
    ResponseEntity aliPayRefund(@PathVariable("outTradeNo") String outTradeNo, @RequestBody Refund refund);

    /**********支付宝*******************************/

    /**********微信*******************************/
    //支付 - 签名
    @RequestMapping(value = "/wx-pay/sign", method = RequestMethod.POST)
    ResponseEntity<String> wxSign(@Valid @RequestBody PaySign sign);

    //支付 - 验签
    @RequestMapping(value = "/wx-pay/{outTradeNo}/verification", method = RequestMethod.POST)
    ResponseEntity wxVerify(@PathVariable("outTradeNo") String outTradeNo, @RequestBody Map<String, String> notifiedParameters);

    //支付 - 退款（支持部分、多次退款)
    @RequestMapping(value = "/wx-pay/{outTradeNo}/refund", method = RequestMethod.PUT)
    ResponseEntity wxRefund(@PathVariable("outTradeNo") String outTradeNo, @RequestBody Refund refund);

    /**********微信*******************************/


    /**********修改支付日志信息*************************/

    //修改支付日志状态
    @RequestMapping(value = "/pay-logs/{outTradeNo}/completed", method = RequestMethod.PUT)
    ResponseEntity completed(@PathVariable("outTradeNo") String outTradeNo, @RequestBody Payed payed);
    /**********修改支付日志信息************************/
}