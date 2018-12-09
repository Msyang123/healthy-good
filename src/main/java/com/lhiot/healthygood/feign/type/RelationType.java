package com.lhiot.healthygood.feign.type;

import lombok.Getter;

/**
 * 广告关联类别枚举
 *
 * @author xiaojian  created in  2018/12/7 15:34
 */
public enum RelationType {
    PRODUCT_DETAILS("商品详情"),
    STORE_LIVE_TELECAST("门店直播"),
    MORE_AMUSEMENT("多娱"),
    EXTERNAL_LINKS("外部链接");


    @Getter
    private String description;

    RelationType(String description) {
        this.description = description;
    }
}
