package com.lhiot.healthygood.type;

public enum CustomOrderBuyType {
    MANUAL("手动"),
    AUTO("自动");
    private final String displayTag;

    public String getDisplayTag(){
        return this.displayTag;
    }

    CustomOrderBuyType(String displayTag){
        this.displayTag = displayTag;
    }
}