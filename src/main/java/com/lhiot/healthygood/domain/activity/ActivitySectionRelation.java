package com.lhiot.healthygood.domain.activity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author hufan created in 2018/12/3 18:52
 **/
@Data
@ToString(callSuper = true)
@ApiModel
@NoArgsConstructor
public class ActivitySectionRelation {
    @ApiModelProperty(notes = "主键id", dataType = "Long")
    private Long id;
    @ApiModelProperty(notes = "活动id", dataType = "Long")
    private Long activityId;
    @ApiModelProperty(notes = "板块id", dataType = "Long")
    private Long sectionId;
}