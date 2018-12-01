package com.lhiot.healthygood.feign.model;

import com.leon.microx.util.Maps;
import com.leon.microx.util.auditing.Random;
import com.lhiot.healthygood.type.FreeSignName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

/**
 * @author Leon (234239150@qq.com) created in 12:25 18.9.14
 */
@Data
@ApiModel
@ToString
public class CaptchaParam implements PayloadConverter {

    @NotBlank(message = "手机号不能为空")
    @ApiModelProperty(notes = "接收短信的手机号", required = true, dataType = "String")
    private String phoneNumber;

    @ApiModelProperty(notes = "模版签名（固定值。枚举: 恰果果, 水果熟了微商城, 视食, 水果熟了）", required = true, dataType = "FreeSignName", allowableValues = "LH_QGG, SGSL_WX_SHOP, FOOD_SEE, SGSL")
    private FreeSignName freeSignName;

    @NotBlank(message = "应用名称不能为空")
    @ApiModelProperty(notes = "短信中显示的应用名称（一般是中文）", required = true, dataType = "String")
    private String applicationName;

    @ApiModelProperty(notes = "扩展参数（一般是JSON格式的字符串）", dataType = "String")
    private String extend;

    @Override
    public Payload toPayload() {
        Payload payload = new Payload(phoneNumber, freeSignName, extend);
        payload.withVars(() -> Maps.of("product", this.applicationName, "code", String.valueOf(Random.ofInt(6))));
        return payload;
    }
}
