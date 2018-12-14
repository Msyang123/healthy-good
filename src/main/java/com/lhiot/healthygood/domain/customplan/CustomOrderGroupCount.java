package com.lhiot.healthygood.domain.customplan;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 订单分组统计对象
 * @author yj
 */
@Data
@ApiModel
public class CustomOrderGroupCount {
    @ApiModelProperty(notes = "待付款", dataType = "int")
    private int waitPaymentCount;
    @ApiModelProperty(notes = "定制中", dataType = "int")
    private int customingCount;
}
