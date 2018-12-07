package com.lhiot.healthygood.domain.activity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@ApiModel("新品尝鲜活动")
@NoArgsConstructor
public class NewSpecialResult {
    @ApiModelProperty(notes="新品尝鲜活动ID",dataType="Long")
    private Long id;

    @ApiModelProperty(notes="活动Id",dataType="Long")
    private Long activityId;

    @ApiModelProperty(notes="活动名称",dataType="String")
    private String activityName;

    @ApiModelProperty(notes="活动描述",dataType="String")
    private String description;

    @ApiModelProperty(notes="限购数量",dataType="Integer")
    private Integer limitCount;

    private List<ActivityProducts> activityProductList;
}
