package com.lhiot.healthygood.domain.customplan.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lhiot.healthygood.domain.customplan.CustomPlan;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author hufan created in 2018/11/30 14:40
 **/
@Data
@ToString(callSuper = true)
@ApiModel
@NoArgsConstructor
public class CustomPlanSectionRelationResult {

    /**
     * id
     */
    @JsonProperty("id")
    @ApiModelProperty(value = "id", dataType = "Long", readOnly = true)
    private Long id;

    /**
     * 定制计划板块id
     */
    @JsonProperty("sectionId")
    @ApiModelProperty(value = "定制计划板块id", dataType = "Long")
    private Long sectionId;

    /**
     * 定制计划id
     */
    @JsonProperty("planId")
    @ApiModelProperty(value = "定制计划id", dataType = "Long")
    private Long planId;

    /**
     * 定制计划排序
     */
    @JsonProperty("sort")
    @ApiModelProperty(value = "定制计划排序", dataType = "Long")
    private Long sort;

    @ApiModelProperty(value = "定制计划列表", dataType = "CustomPlan")
    private CustomPlan customPlan;
}