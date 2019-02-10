package com.lhiot.healthygood.type;

public enum BalanceType {
    BOUNS("红利余额"),
    SETTLEMENT("可结算余额");
    private final String displayTag;

    public String getDisplayTag(){
        return this.displayTag;
    }
    BalanceType(String displayTag) {
        this.displayTag = displayTag;
    }
}
