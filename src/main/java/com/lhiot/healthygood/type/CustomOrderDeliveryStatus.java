package com.lhiot.healthygood.type;

import lombok.Getter;

/**
 * 个人购买定制计划领取订单状态
 */
public enum CustomOrderDeliveryStatus {
    DISPATCHING("配送中"),
    RECEIVED("已收货"),
    RETURNING("退货中"),
    ALREADY_RETURN("退货完成"),
    FINISHED("完成");

    @Getter
    private String description;

    CustomOrderDeliveryStatus(String description) {
        this.description = description;
    }
}
