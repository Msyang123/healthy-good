package com.lhiot.healthygood.domain.customplan;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
* Description:定制计划规格实体类
* @author zhangs
* @date 2018/11/22
*/
@Data
@ApiModel
public class CustomPlanSpecification{

    /**
    *
    */
    @JsonProperty("id")
    @ApiModelProperty(value = "", dataType = "Long")
    private Long id;

    /**
    *定制计划id
    */
    @JsonProperty("planId")
    @ApiModelProperty(value = "定制计划id", dataType = "Long")
    private Long planId;

    /**
    *价格
    */
    @JsonProperty("price")
    @ApiModelProperty(value = "价格", dataType = "Integer")
    private Integer price;

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
    *定制计划周期
    */
    @JsonProperty("planPeriod")
    @ApiModelProperty(value = "定制计划周期", dataType = "Integer")
    private Integer planPeriod;

    @JsonProperty("image")
    @ApiModelProperty(value = "定制说明配图", dataType = "String")
    private String image;

    @JsonProperty("standardId")
    @ApiModelProperty(value = "定制规格基础id", dataType = "Long")
    private Long standardId;



}
