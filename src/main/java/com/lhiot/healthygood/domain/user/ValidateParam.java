package com.lhiot.healthygood.domain.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

/**
 * @author Leon (234239150@qq.com) created in 9:48 18.9.15
 */
@Data
@ApiModel
@ToString
public class ValidateParam {
    @NotBlank(message = "手机号不能为空")
    @ApiModelProperty(notes = "接收短信的手机号", required = true, dataType = "String")
    private String phoneNumber;

    @NotBlank(message = "验证码不能为空")
    @ApiModelProperty(notes = "要校验的验证码", required = true, dataType = "String")
    private String code;
}
