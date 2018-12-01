package com.lhiot.healthygood.feign.type;

import lombok.Getter;

/**
 * 应用类型
 */
public enum ApplicationType {

    FRUIT_DOCTOR("和色果膳");
    @Getter
    private String description;

    ApplicationType(String description) {
        this.description = description;
    }
}
