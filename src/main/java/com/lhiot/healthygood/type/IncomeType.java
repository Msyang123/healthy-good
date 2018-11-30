package com.lhiot.healthygood.type;

public enum IncomeType {
    INCOME("收入"),
    EXPENDITURE("支出"),
    DEFAULT("全部");
    private final String displayTag;

    public String getDisplayTag(){
        return this.displayTag;
    }

    IncomeType(String displayTag){
        this.displayTag = displayTag;
    }
}