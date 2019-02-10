package com.lhiot.healthygood.type;

public enum OperStatus {
    PAUSE("暂停"),
    RECOVERY("恢复");
    private final String displayTag;

    public String getDisplayTag(){
        return this.displayTag;
    }

    OperStatus(String displayTag){
        this.displayTag = displayTag;
    }
}