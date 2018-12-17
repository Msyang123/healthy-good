package com.lhiot.healthygood.util;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@lombok.Data
@ApiModel
@NoArgsConstructor
@AllArgsConstructor
public class DataObject {
    private String value;
    private String color = "#000000";

    @Override
    public String toString() {
        return "{\"value\":\""+value+"\",\"color\":\""+color+"\"}";
    }


}
