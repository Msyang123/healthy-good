package com.lhiot.healthygood.feign.type;

public enum PayType {
    WE_CHAT("微信"),
    ALIPAY("支付宝"),
    BALANCE("余额")
    ;

    private String description;

    PayType(String description) {
        this.description = description;
    }
}