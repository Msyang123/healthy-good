package com.lhiot.healthygood.feign;

import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tuple;
import com.lhiot.healthygood.feign.model.CreateOrderParam;
import com.lhiot.healthygood.feign.model.OrderDetailResult;
import com.lhiot.healthygood.feign.model.ReturnOrderParam;
import com.lhiot.healthygood.feign.type.OrderStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

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

    //订单退货(包括部分和全部)
    @RequestMapping(value = "/orders/{orderCode}/refund", method = RequestMethod.PUT)
    ResponseEntity refundOrder(@PathVariable("orderCode") String orderCode, @NotNull @RequestBody ReturnOrderParam returnOrderParam);


    @RequestMapping(value = "/orders/user/{userId}", method = RequestMethod.GET)
    ResponseEntity<Tuple<OrderDetailResult>> ordersByUserId(@PathVariable("userId") Long userId,
                                  @RequestParam(value = "orderType", required = false) String orderType,
                                  @RequestParam(value = "orderStatus", required = false) OrderStatus orderStatus);
}