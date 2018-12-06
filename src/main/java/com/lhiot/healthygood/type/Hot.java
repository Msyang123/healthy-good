package com.lhiot.healthygood.type;

public enum Hot {
    NO("普通鲜果师"),
    YES("明星鲜果师");
    private final String displayTag;

    public String getDisplayTag(){
        return this.displayTag;
    }

    Hot(String displayTag){
        this.displayTag = displayTag;
    }
}