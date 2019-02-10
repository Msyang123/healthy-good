package com.lhiot.healthygood.type;

import lombok.Getter;

/**
 * @author Leon (234239150@qq.com) created in 14:54 18.9.14
 */
public enum FreeSignName {

    LH_QGG("恰果果"),

    SGSL_WX_SHOP("水果熟了微商城"),

    FOOD_SEE("视食"),

    SGSL("水果熟了"),

    FRUIT_DOCTOR("和色果膳"),

    ;
    @Getter
    private String desc;

    FreeSignName(String desc) {
        this.desc = desc;
    }
}
