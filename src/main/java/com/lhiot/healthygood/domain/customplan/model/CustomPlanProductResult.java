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
    @ApiModelProperty(value = "", dataType = "Long")
    private Long id;

    /**
     *名称
     */
    @JsonProperty("productName")
    @ApiModelProperty(value = "状态", dataType = "String")
    private String productName;

    /**
     *上架Id
     */
    @JsonProperty("productShelfId")
    @ApiModelProperty(value = "上架Id", dataType = "Long")
    private Long productShelfId;

    /**
     *名称
     */
    @JsonProperty("dayN")
    @ApiModelProperty(value = "", dataType = "String")
    private String dayN;


    /**
     *planId
     */
    @JsonProperty("planId")
    @ApiModelProperty(value = "", dataType = "Long")
    private Long planId;

    /**
     *描述
     */
    @JsonProperty("description")
    @ApiModelProperty(value = "", dataType = "String")
    private String description;

    /**
     *图片
     */
    @JsonProperty("productImage")
    @ApiModelProperty(value = "", dataType = "String")
    private String productImage;
}
