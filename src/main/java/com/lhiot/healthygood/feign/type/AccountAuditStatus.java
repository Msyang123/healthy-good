package com.lhiot.healthygood.feign.type;

import lombok.Getter;

public enum AccountAuditStatus {
    PASSED("通过"),
    FAILED("未通过");

    @Getter
    private String desc;

    AccountAuditStatus(String desc) {
        this.desc = desc;
    }
}
