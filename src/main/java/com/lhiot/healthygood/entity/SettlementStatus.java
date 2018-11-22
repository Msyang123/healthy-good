package com.lhiot.healthygood.entity;

public enum SettlementStatus {
    UNSETTLED("未处理"),
    SUCCESS("已成功"),
    EXPIRED("已过期");
    private final String displayTag;

    public String getDisplayTag(){
        return this.displayTag;
    }

    SettlementStatus(String displayTag){
        this.displayTag = displayTag;
    }
}