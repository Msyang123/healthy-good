package com.lhiot.healthygood.type;

public enum AuditStatus {
    UNAUDITED("待审核"),
    AGREE("审核通过"),
    REJECT("审核不通过");
    private final String displayTag;

    public String getDisplayTag(){
        return this.displayTag;
    }

    AuditStatus(String displayTag){
        this.displayTag = displayTag;
    }
}