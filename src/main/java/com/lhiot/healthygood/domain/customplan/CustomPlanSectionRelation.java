package com.lhiot.healthygood.domain.customplan;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
* Description:定制计划板块关联定制计划实体类
* @author zhangs
* @date 2018/11/22
*/
@Data
@ToString(callSuper = true)
@ApiModel
@NoArgsConstructor
public class CustomPlanSectionRelation{

    /**
    *id
    */
    @JsonProperty("id")
    @ApiModelProperty(value = "id", dataType = "Long")
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
    *是否推荐计划
    */
    @JsonProperty("sort")
    @ApiModelProperty(value = "是否推荐计划", dataType = "Long")
    private Long sort;

}
