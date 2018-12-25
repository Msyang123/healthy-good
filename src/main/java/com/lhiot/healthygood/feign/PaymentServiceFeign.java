package com.lhiot.healthygood.feign;

import com.leon.microx.web.result.Id;
import com.lhiot.healthygood.feign.model.*;
import io.swagger.annotations.ApiOperation;
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
    //支付签名 本服务支付宝方面 目前只支持原生APP支付
    @RequestMapping(value = "/ant-financial/app/sign", method = RequestMethod.POST)
    ResponseEntity<String> aliPaySign(@Valid @RequestBody AliPayModel aliPay);

    //支付 - 验签 第三方回调参数请解析为Map后原样传递到此接口完成签名校验
    @RequestMapping(value = "/ant-financial/payed/{outTradeNo}/verification", method = RequestMethod.POST)
    ResponseEntity aliPayVerify(@PathVariable("outTradeNo") String outTradeNo, @RequestBody Map<String, String> notifiedParameters);

    //支付 - 撤销 【一般用于回调异常】 如果已支付成功，则第三方自动退款，如果未支付，则第三方取消本次支付。
    @RequestMapping(value = "/ant-financial/payed/{outTradeNo}", method = RequestMethod.DELETE)
    ResponseEntity aliPayCancel(@PathVariable("outTradeNo") String outTradeNo);

    /**********支付宝*******************************/

    /**********微信*******************************/
    //支付 - app签名
    @RequestMapping(value = "/we-chat/app/sign", method = RequestMethod.POST)
    ResponseEntity<Map> wxAppSign(@Valid @RequestBody WxPayModel wxPay);

    //支付 -jssdk签名
    @RequestMapping(value = "/we-chat/js-api/sign", method = RequestMethod.POST)
    ResponseEntity<Map> wxJsSign(@Valid @RequestBody WxPayModel wxPay);

    //支付 - 验签
    @RequestMapping(value = "/we-chat/payed/{outTradeNo}/verification", method = RequestMethod.POST)
    ResponseEntity wxVerify(@PathVariable("outTradeNo") String outTradeNo, @RequestBody Map<String, String> notifiedParameters);
    /**********微信*******************************/

    /**********余额支付*******************************/

    //余额支付
    @RequestMapping(value ="/balance/payments",method = RequestMethod.POST)
    ResponseEntity<Id> balancePay(@Valid @RequestBody BalancePayModel balancePay);
    /**********余额支付*******************************/



    /**********修改支付日志信息*************************/

    //修改支付单为完成状态
    @RequestMapping(value = "/records/{outTradeNo}/completed", method = RequestMethod.PUT)
    ResponseEntity completed(@PathVariable("outTradeNo") String outTradeNo, @RequestBody PayedModel payed);


    //按支付单号查询一个支付记录
    @RequestMapping(value = "/records/{outTradeNo}", method = RequestMethod.GET)
    ResponseEntity<Record> one(@Valid @PathVariable("outTradeNo") String outTradeNo);
    /**********修改支付日志信息************************/

    /***********支付退款***********************************/
    //支付 - 退款（支持部分、多次退款）
    @RequestMapping(value = "/payed/{outTradeNo}/refunds", method = RequestMethod.POST)
    ResponseEntity refund(@PathVariable("outTradeNo") String outTradeNo, @Valid @RequestBody RefundModel refund);

    @RequestMapping(value = "/refunds/{outRefundNo}/completed", method = RequestMethod.PUT)
    @ApiOperation("修改退款单为完成状态 退款回调中调用此接口 参数为微信回调中的out_refund_no")
    ResponseEntity completed(@PathVariable("outRefundNo") String outRefundNo);
    /************支付退款*****************************/
}