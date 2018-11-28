package com.lhiot.healthygood.domain.customplan;


import com.fasterxml.jackson.annotation.JsonProperty;

import com.lhiot.healthygood.common.PagerRequestObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
* Description:定制计划板块关联定制计划实体类
* @author zhangs
* @date 2018/11/22
*/
@Data
@ToString(callSuper = true)
@ApiModel
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CustomPlanSectionRelation extends PagerRequestObject {

    /**
    *id
    */
    @JsonProperty("id")
    @ApiModelProperty(value = "id", dataType = "Long", readOnly = true)
    private Long id;

    /**
    *定制计划板块id
    */
    @JsonProperty("sectionId")
    @ApiModelProperty(value = "定制计划板块id", dataType = "Long")
    private Long sectionId;

    /**
    *定制计划id
    */
    @JsonProperty("planId")
    @ApiModelProperty(value = "定制计划id", dataType = "Long")
    private Long planId;

    /**
    *定制计划排序
    */
    @JsonProperty("sort")
    @ApiModelProperty(value = "定制计划排序", dataType = "Long")
    private Long sort;

    @ApiModelProperty(value = "定制计划列表", dataType = "CustomPlan")
    private CustomPlan customPlan;
}
