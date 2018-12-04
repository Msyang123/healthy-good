package com.lhiot.healthygood.type;

import lombok.Getter;

/**
 * 是否有效
 */
public enum ValidOrInvalid {
    Valid("有效"),
    Invalid("无效");

    @Getter
    private String decription;

    ValidOrInvalid(String decription) {
        this.decription = decription;
    }
}
