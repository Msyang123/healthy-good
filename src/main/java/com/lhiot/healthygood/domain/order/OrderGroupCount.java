package com.lhiot.healthygood.domain.order;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 订单分组统计对象
 * @author yj
 */
@Data
@ApiModel
public class OrderGroupCount {
    @ApiModelProperty(notes = "待付款", dataType = "int")
    private int waitPaymentCount;
    @ApiModelProperty(notes = "待收货", dataType = "int")
    private int waitReceiveCount;
}
