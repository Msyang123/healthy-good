package com.lhiot.healthygood.feign;

import com.lhiot.healthygood.domain.order.CreateOrderParam;
import com.lhiot.healthygood.domain.order.OrderDetailResult;
import org.springframework.http.ResponseEntity;

public class OrderServiceHystrix implements OrderServiceFeign{
    @Override
    public ResponseEntity<OrderDetailResult> createOrderWithAssortment(CreateOrderParam orderParam) {
        return null;
    }
}
