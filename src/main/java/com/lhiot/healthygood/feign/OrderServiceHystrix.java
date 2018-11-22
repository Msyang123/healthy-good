package com.lhiot.healthygood.feign;

import com.lhiot.healthygood.model.order.CreateOrderParam;
import com.lhiot.healthygood.model.order.OrderDetailResult;
import org.springframework.http.ResponseEntity;

public class OrderServiceHystrix implements OrderServiceFeign{
    @Override
    public ResponseEntity<OrderDetailResult> createOrderWithAssortment(CreateOrderParam orderParam) {
        return null;
    }
}
