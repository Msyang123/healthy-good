package com.lhiot.healthygood.feign.type;

import lombok.Getter;

/**
 * 广告类别枚举
 *
 * @author xiaojian  created in  2018/11/21 10:15
 */
public enum AdvertiseType {
    IMAGE("图片广告"),
    TEXT("文字广告");

    @Getter
    private String description;

    AdvertiseType(String description) {
        this.description = description;
    }
}
