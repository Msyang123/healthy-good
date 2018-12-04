package com.lhiot.healthygood.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Component
@FeignClient("PAYMENT-SERVICE-V1-0")
public interface PaymentServiceFeign {
    //支付签名
    @RequestMapping(value = "/sign/", method = RequestMethod.POST)
    ResponseEntity paymentSign();

    //支付回调参数验签
    @RequestMapping(value = "/validate", method = RequestMethod.POST)
    ResponseEntity validateSignParam();
}