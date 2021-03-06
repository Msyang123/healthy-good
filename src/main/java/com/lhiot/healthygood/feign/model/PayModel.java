package com.lhiot.healthygood.feign.model;

import com.lhiot.dc.dictionary.HasEntries;
import com.lhiot.healthygood.feign.type.ApplicationType;
import com.lhiot.healthygood.feign.type.SourceType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ToString
@ApiModel(description = "支付签名参数（父类）")
public class PayModel {

    @HasEntries(from = "applications")
    @NotBlank(message = "应用类型不能为空")
    @ApiModelProperty(value = "应用类型", dataType = "ApplicationType")
    private ApplicationType applicationType;

    @DecimalMin(value = "1", message = "用户编号不能为0")
    @ApiModelProperty(value = "userId", dataType = "Long")
    private Long userId;

    @NotNull(message = "支付类型不能为空")
    @ApiModelProperty(value = "支付类型", dataType = "SourceType")
    private SourceType sourceType;

    @DecimalMin(value = "1", message = "支付金额必须大于0")
    @ApiModelProperty(value = "支付金额(分)", dataType = "Integer")
    private Integer fee;

    @NotBlank(message = "支付项目不能为空")
    @ApiModelProperty(value = "支付项目（描述信息）", dataType = "String")
    private String memo;

    @ApiModelProperty(value = "附加参数（可选）", dataType = "String")
    private String attach;
}
