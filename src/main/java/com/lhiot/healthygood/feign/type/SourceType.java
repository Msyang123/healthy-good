package com.lhiot.healthygood.feign.type;

import lombok.Getter;

public enum SourceType {

    RECHARGE("充值"),

    ORDER("订单"),

    CUSTOM_PLAN("定制订单"),

    ACTIVITY("活动");

    @Getter
    private String description;

    SourceType(String description) {
        this.description = description;
    }
}