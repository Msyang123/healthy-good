package com.lhiot.healthygood.type;

import lombok.Getter;

/**
 * 订单状态
 */
public enum CustomOrderStatus {
    WAIT_PAYMENT("待支付"),
    INVALID("已失效"),
    PAUSE_DELIVERY("暂停配送"),
    CUSTOMING("定制中"),
    FINISHED("已结束");

    @Getter
    private String description;

    CustomOrderStatus(String description) {
        this.description = description;
    }
}
