package com.lhiot.healthygood.domain.customplan;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
* Description:定制计划规格基础数据实体类
* @author hufan
* @date 2018/12/08
*/
@Data
@ToString(callSuper = true)
@ApiModel
@NoArgsConstructor
public class CustomPlanSpecificationStandard{

    /**
    *
    */
    @JsonProperty("id")
    @ApiModelProperty(value = "规格基础id", dataType = "Long")
    private Long id;

    /**
    *数量
    */
    @JsonProperty("quantity")
    @ApiModelProperty(value = "数量", dataType = "Integer")
    private Integer quantity;

    /**
    *描述
    */
    @JsonProperty("description")
    @ApiModelProperty(value = "描述", dataType = "String")
    private String description;

    /**
    *定制说明配图
    */
    @JsonProperty("image")
    @ApiModelProperty(value = "定制说明配图", dataType = "String")
    private String image;

}
