package com.lhiot.healthygood.domain.activity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@ApiModel("新品尝鲜活动")
@NoArgsConstructor
public class ActivityProductRecord {
    @ApiModelProperty(notes="ID",dataType="Long")
    private Long id;

    @ApiModelProperty(notes="userId",dataType="Long")
    private Long userId;

    @ApiModelProperty(notes="商品上架ID",dataType="Long")
    private Long productShelfId;

    @ApiModelProperty(notes="订单编号",dataType="Long")
    private String orderCode;

    @ApiModelProperty(notes="活动ID",dataType="Long")
    private Long activityId;

    @ApiModelProperty(notes="活动类型",dataType="Long")
    private String activityType;

}
