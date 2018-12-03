package com.lhiot.healthygood.feign.type;

import lombok.Getter;

/**
 * 广告类别枚举
 *
 * @author xiaojian  created in  2018/11/21 10:15
 */
public enum AdvertiseType {
    PRODUCT_DETAILS("商品详情"),
    STORE_LIVE_TELECAST("门店直播"),
    MORE_AMUSEMENT("多娱"),
    EXTERNAL_LINKS("外部链接");


    @Getter
    private String description;

    AdvertiseType(String description) {
        this.description = description;
    }
}
