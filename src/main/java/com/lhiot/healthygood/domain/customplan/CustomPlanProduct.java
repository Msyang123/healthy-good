package com.lhiot.healthygood.domain.customplan;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
* Description:定制计划关联商品实体类
* @author hufan
* @date 2018/11/26
*/
@Data
@ApiModel
public class CustomPlanProduct{
    @ApiModelProperty(value = "id", dataType = "Long")
    private Long id;
    @ApiModelProperty(value = "定制计划id", dataType = "Long")
    private Long planId;
    @ApiModelProperty(value = "上架Id", dataType = "Long")
    private Long productShelfId;
    @ApiModelProperty(value = "第x天", dataType = "Integer")
    private Integer dayN;
    @ApiModelProperty(value = "定制周期（周、月）", dataType = "String")
    private String planPeriod;
    @ApiModelProperty(value = "序号", dataType = "Integer")
    private Integer sort;

}
