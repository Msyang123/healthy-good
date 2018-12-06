package com.lhiot.healthygood.type;

public enum DoctorStatus {
    VALID("正常"),
    INVALID("已停用");
    private final String displayTag;

    public String getDisplayTag(){
        return this.displayTag;
    }

    DoctorStatus(String displayTag){
        this.displayTag = displayTag;
    }
}