package com.lhiot.healthygood.feign.type;

import lombok.Getter;

/**
 * 文章类型枚举
 *
 * @author xiaojian  created in  2018/11/22 8:57
 */
public enum ArticleType {
    ORIGINAL("原创"),
    REPRINTED("转载");


    @Getter
    private String description;

    ArticleType(String description) {
        this.description = description;
    }
}
