package com.lhiot.healthygood.type;

public enum DoctorLevel {
    TRAINING("培训中"),
    PRIMARY("初级"),
    SENIOR("中高级");
    private final String displayTag;

    public String getDisplayTag(){
        return this.displayTag;
    }

    DoctorLevel(String displayTag){
        this.displayTag = displayTag;
    }
}