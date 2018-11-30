package com.lhiot.healthygood.feign.type;

import lombok.Getter;

/**
 * 门店状态枚举
 */
public enum StoreStatus {
    ENABLED("营业"),
    DISABLED("未营业");


    @Getter
    private String  decription;

    StoreStatus(String decription) {
        this.decription = decription;
    }
}
