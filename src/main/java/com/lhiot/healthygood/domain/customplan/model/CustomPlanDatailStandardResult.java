package com.lhiot.healthygood.domain.customplan.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
@Data
@ToString(callSuper = true)
@ApiModel
@NoArgsConstructor
public class CustomPlanDatailStandardResult {
    /**
     *周期
     */
    @JsonProperty("planPeriod")
    @ApiModelProperty(value = "定制周期（7-周 30-月）", dataType = "String")
    private String planPeriod;
    /**
     *规格List
     */
    @JsonProperty("specificationList")
    @ApiModelProperty(value = "定制规格集合", dataType = "List")
    private List<CustomPlanSpecificationResult> specificationList;
    /**
     *产品
     */
    @JsonProperty("products")
    @ApiModelProperty(value = "定制商品集合", dataType = "List")
    private List<CustomPlanProductResult> products;
}
