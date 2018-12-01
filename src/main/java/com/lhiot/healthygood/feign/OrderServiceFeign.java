package com.lhiot.healthygood.feign;

import com.lhiot.healthygood.feign.model.CreateOrderParam;
import com.lhiot.healthygood.feign.model.OrderDetailResult;
import com.lhiot.healthygood.feign.type.OrderStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@Component
@FeignClient("BASE-ORDER-SERVICE-V1-0")
public interface OrderServiceFeign {

    //创建订单
    @RequestMapping(value = "/orders/", method = RequestMethod.POST)
    ResponseEntity<OrderDetailResult> createOrder(@RequestBody CreateOrderParam orderParam);

    //修改订单状态为已发货
    @RequestMapping(value = "/orders/{orderCode}/status", method = RequestMethod.PUT)
    ResponseEntity updateOrderStatus(@PathVariable("orderCode") String orderCode, @RequestParam("orderStatus") OrderStatus orderStatus);

    @RequestMapping(value = "/orders/{orderCode}", method = RequestMethod.GET)
    ResponseEntity<OrderDetailResult> orderDetail(@PathVariable("orderCode") String orderCode, @RequestParam("needProductList") boolean needProductList,
                                                  @RequestParam("needOrderFlowList") boolean needOrderFlowList);
}