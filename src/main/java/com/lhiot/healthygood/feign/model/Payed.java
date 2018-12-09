package com.lhiot.healthygood.feign.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Date;

/**
 * @author Leon (234239150@qq.com) created in 11:35 18.12.3
 */
@Data
@ApiModel
public class Payed {

    @ApiModelProperty(notes = "第三方支付产生的商户单号(我们产生的)",dataType = "String")
    private String payId;

    @ApiModelProperty(notes = "银行类型",dataType = "String")
    private String bankType;

    @ApiModelProperty(notes = "支付时间",dataType = "Date")
    private Date payAt;

    @ApiModelProperty(notes = "支付平台交易号(支付平台产生的)",dataType = "String")
    private String tradeId;
}