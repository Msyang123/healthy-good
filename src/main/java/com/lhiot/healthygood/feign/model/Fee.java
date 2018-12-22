package com.lhiot.healthygood.feign.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class Fee {

    @ApiModelProperty(value = "金额(分)", dataType = "Integer")
    private Integer fee;
}
