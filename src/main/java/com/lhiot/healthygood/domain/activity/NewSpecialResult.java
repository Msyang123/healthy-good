package com.lhiot.healthygood.domain.activity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@ApiModel("新品尝鲜活动")
@NoArgsConstructor
public class NewSpecialResult {
    @ApiModelProperty(notes="活动ID",dataType="Long")
    private Long id;

    @ApiModelProperty(notes="活动名称",dataType="Long")
    private String activityName;

    @ApiModelProperty(notes="活动描述",dataType="Long")
    private String description;

    @ApiModelProperty(notes="限购数量",dataType="Long")
    private Integer limitCount;

    private List<ActivityProducts> activityProductsList;
}
