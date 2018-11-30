package com.lhiot.healthygood.domain.customplan;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
* Description:定制计划关联商品实体类
* @author zhangs
* @date 2018/11/26
*/
@Data
@ToString(callSuper = true)
@ApiModel
@NoArgsConstructor
public class CustomPlanProduct{

    /**
    *id
    */
    @JsonProperty("id")
    @ApiModelProperty(value = "id", dataType = "Long")
    private Long id;

    /**
    *定制计划id
    */
    @JsonProperty("planId")
    @ApiModelProperty(value = "定制计划id", dataType = "Long")
    private Long planId;

    /**
    *上架Id
    */
    @JsonProperty("productShelfId")
    @ApiModelProperty(value = "上架Id", dataType = "Long")
    private Long productShelfId;

    /**
    *第x天
    */
    @JsonProperty("dayN")
    @ApiModelProperty(value = "第x天", dataType = "Integer")
    private Integer dayN;

    /**
    *定制周期（周、月）
    */
    @JsonProperty("planPeriod")
    @ApiModelProperty(value = "定制周期（周、月）", dataType = "String")
    private String planPeriod;

    /**
    *序号
    */
    @JsonProperty("sort")
    @ApiModelProperty(value = "序号", dataType = "Integer")
    private Integer sort;

}
