package com.lhiot.healthygood.feign;

import com.lhiot.healthygood.domain.order.CreateOrderParam;
import com.lhiot.healthygood.domain.order.OrderDetailResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient("BASE-ORDER-SERVICE-V1-0")
public interface OrderServiceFeign {

    /**
     * 更新公共用户鲜果币
     */
    @RequestMapping(value="/orders",method = RequestMethod.POST)
    ResponseEntity<OrderDetailResult> createOrderWithAssortment(CreateOrderParam orderParam);

    //    public ResponseEntity createOrderWithAssortment(@RequestBody CreateOrderParam orderParam) {
}