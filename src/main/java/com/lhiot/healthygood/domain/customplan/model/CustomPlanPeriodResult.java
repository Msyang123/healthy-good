package com.lhiot.healthygood.domain.customplan.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lhiot.healthygood.domain.customplan.CustomPlanSpecification;
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
/**
 * 周期类型
 */
public class CustomPlanPeriodResult {
    /**
     *周期
     */
    @JsonProperty("planPeriod")
    @ApiModelProperty(value = "定制周期（7-周 30-月）", dataType = "Integer")
    private Integer planPeriod;

    /**
     *规格List
     */
    @JsonProperty("specificationList")
    @ApiModelProperty(value = "定制规格集合", dataType = "List")
    private List<CustomPlanSpecification> specificationList;
    /**
     *商品List
     */
    @JsonProperty("products")
    @ApiModelProperty(value = "定制商品集合", dataType = "List")
    private List<CustomPlanProductResult> products;

}
