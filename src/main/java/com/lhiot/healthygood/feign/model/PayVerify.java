package com.lhiot.healthygood.feign.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Map;

/**
 * @author Leon (234239150@qq.com) created in 10:08 18.12.3
 */
@Data
@ApiModel
public class PayVerify {

    @NotBlank(message = "商户订单号不能为空")
    @ApiModelProperty(value = "商户订单号（签名时生成）", dataType = "String", required = true)
    private String outTradeNo;

    @NotEmpty(message = "支付回调验签参数不能为空")
    @ApiModelProperty(value = "支付回调验签参数(原样传递)", dataType = "Map", required = true)
    private Map<String, String> notifyParams;
}
