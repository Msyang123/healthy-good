package com.lhiot.healthygood.feign.type;

import lombok.Getter;

/**
 * 文章发布状态枚举
 *
 * @author xiaojian  created in  2018/11/22 9:01
 */
public enum ArticleStatus {
    PUBLISH("发布"),
    UN_PUBLISH("未发布");


    @Getter
    private String description;

    ArticleStatus(String description) {
        this.description = description;
    }
}
