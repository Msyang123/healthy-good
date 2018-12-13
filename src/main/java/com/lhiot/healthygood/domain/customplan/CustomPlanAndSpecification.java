package com.lhiot.healthygood.domain.customplan;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
* Description:定制计划和定制计划规格
* @author yj
* @date 2018/11/22
*/
@Data
@ApiModel
public class CustomPlanAndSpecification {

    /**
     * 定制计划规格
     */
    private CustomPlanSpecification customPlanSpecification;

    /**
     * 定制计划对象
     */
    @ApiModelProperty(value = "定制计划对象", dataType = "CustomPlan")
    private CustomPlan customPlan;
}
