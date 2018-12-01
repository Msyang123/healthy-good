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
public class CustomPlanProductResult {
    /**
     *
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
    @JsonProperty("productShelfId")
    @ApiModelProperty(value = "商品上架Id", dataType = "Long")
    private Long productShelfId;

    /**
     *名称
     */
    @JsonProperty("dayN")
    @ApiModelProperty(value = "第x天（如：第1天则为1 纯数字）", dataType = "String")
    private String dayN;


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
    @JsonProperty("productImage")
    @ApiModelProperty(value = "商品图片", dataType = "String")
    private String productImage;
}
