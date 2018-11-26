package com.lhiot.healthygood.domain.customplan.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class CustomPlanDatailStandardResult {
    /**
     *描述
     */
    @JsonProperty("planPeriod")
    @ApiModelProperty(value = "planPeriod", dataType = "String")
    private String planPeriod;
    /**
     *描述
     */
    @JsonProperty("specificationList")
    @ApiModelProperty(value = "planPeriod", dataType = "List")
    private List<CustomPlanSpecificationResult> specificationList;



}
