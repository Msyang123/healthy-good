package com.lhiot.healthygood.type;

import lombok.Getter;

/**
 * @author hufan created in 2019/1/3 16:45
 **/
public enum OptionType {
    INSERT("新增操作"),
    UPDATE("修改操作");

    @Getter
    private String decription;

    OptionType(String decription) {
        this.decription = decription;
    }
}
