package com.lhiot.healthygood.feign.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMin;

/**
 * @author Leon (234239150@qq.com) created in 11:53 18.12.5
 */
@Data
@ApiModel
public class Refund {

    @DecimalMin(value = "1", message = "支付金额必须大于0")
    @ApiModelProperty(value = "支付金额(分)", dataType = "Long", required = true)
    private Long fee;

    @ApiModelProperty(value = "退款原因", dataType = "String")
    private String reason;

    @ApiModelProperty(value = "退款回调地址", dataType = "String")
    private String notifyUrl;
}
