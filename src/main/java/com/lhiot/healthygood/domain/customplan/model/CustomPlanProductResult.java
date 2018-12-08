package com.lhiot.healthygood.domain.customplan.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@ApiModel
@NoArgsConstructor
/**
 * 定制计划商品信息
 */
public class CustomPlanProductResult {
    /**
     *定制商品id
     */
    @JsonProperty("id")
    @ApiModelProperty(value = "定制商品id", dataType = "Long")
    private Long id;

    /**
     *名称
     */
    @JsonProperty("productName")
    @ApiModelProperty(value = "商品名称", dataType = "String")
    private String productName;

    /**
     *上架Id
     */
    @JsonProperty("shelfId")
    @ApiModelProperty(value = "商品上架Id", dataType = "Long")
    private Long productShelfId;

    /**
     *第x天
     */
    @JsonProperty("dayOfPeriod")
    @ApiModelProperty(value = "第x天（如：第1天则为1 纯数字）", dataType = "Integer")
    private Integer dayOfPeriod;


    /**
     *planId
     */
    @JsonProperty("planId")
    @ApiModelProperty(value = "定制计划id", dataType = "Long")
    private Long planId;

    /**
     *描述
     */
    @JsonProperty("description")
    @ApiModelProperty(value = "描述", dataType = "String")
    private String description;

    /**
     *图片
     */
    @JsonProperty("image")
    @ApiModelProperty(value = "商品图片", dataType = "String")
    private String image;

    /**
     * 益处
     */
    @JsonProperty("benefit")
    @ApiModelProperty(value = "益处", dataType = "String")
    private String benefit;
}
