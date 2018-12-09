package com.lhiot.healthygood.feign.type;

import lombok.Getter;

/**
 * @author xiaojian  created in  2018/11/20 16:12
 */
public enum PositionType {
    PRODUCT("商品"),
    ADVERTISEMENT("广告"),
    ARTICLE("文章");

    @Getter
    private String description;

    PositionType(String description) {
        this.description = description;
    }
}
