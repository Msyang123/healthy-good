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
public class CustomPlanSpecificationDetailResult {
    /**
     *
     */
    @JsonProperty("id")
    @ApiModelProperty(value = "定制规格id", dataType = "Long")
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
    @ApiModelProperty(value = "价格", dataType = "String")
    private String price;

    /**
     *数量
     */
    @JsonProperty("quantity")
    @ApiModelProperty(value = "数量", dataType = "String")
    private String quantity;

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
    @ApiModelProperty(value = "定制计划周期", dataType = "String")
    private String planPeriod;


}
