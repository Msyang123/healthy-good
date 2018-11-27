package com.lhiot.healthygood.entity;

import lombok.Getter;

/**
 * @author zhangfeng create in 15:43 2018/11/8
 */
public enum ShelfType {
    NORMAL("普通商品"),
    GIFT("赠品")
    ;

    @Getter
    private String description;

    ShelfType(String description) {
        this.description = description;
    }


}
