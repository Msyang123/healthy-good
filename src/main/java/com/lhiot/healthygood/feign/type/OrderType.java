package com.lhiot.healthygood.feign.type;

import lombok.Getter;

/**
 * 订单状态
 */
public enum OrderType {
    NORMAL("普通订单"),
    CUSTOM("定制订单");

    @Getter
    private String description;

    OrderType(String description) {
        this.description = description;
    }
}
