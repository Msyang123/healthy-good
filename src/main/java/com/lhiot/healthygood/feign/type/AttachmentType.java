package com.lhiot.healthygood.feign.type;

import lombok.Getter;

/**
 * @author xiaojian created in 2018/11/28 11:08
 **/
public enum AttachmentType {
    MAIN_IMG("主图"),
    SUB_IMG("附图"),
    DETAIL_IMG("详情图"),
    ICON("图标");

    @Getter
    private String description;

    AttachmentType(String description) {
        this.description = description;
    }


}
