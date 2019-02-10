package com.lhiot.healthygood.type;

public enum SourceType {
    ORDER("订单分成"),
    SUB_DISTRIBUTOR("分销商分成"),
    SETTLEMENT("红利结算"),
    REFUND("客户退款"),
    SETTLEMENT_REFUND("红利结算退款"),;
    private final String displayTag;

    public String getDisplayTag(){
        return this.displayTag;
    }

    SourceType(String displayTag){
        this.displayTag = displayTag;
    }
}