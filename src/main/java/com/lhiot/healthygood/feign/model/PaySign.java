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

/**
 * @author Leon (234239150@qq.com) created in 14:21 18.11.27
 */
@Data
@ToString
@ApiModel("支付签名参数(微信 支付宝 鲜果币统一)")
public class PaySign {

    @ApiModelProperty(value = "微信openid 支付宝支付不需要", dataType = "String")
    private String openid;

    @NotBlank(message = "客户端IP")
    @ApiModelProperty(value = "订单生成的机器IP，APP和网页支付传用户浏览器端IP，Native支付传调用微信支付API的机器IP", dataType = "String", required = true)
    private String clientIp;

    @ApiModelProperty(value = "附加参数", dataType = "String", required = true)
    private String attach;

    @DecimalMin(value = "1", message = "用户编号不能为0")
    @ApiModelProperty(value = "userId", dataType = "Long", required = true)
    private Long userId;

    @HasEntries(from = "applications")
    @NotBlank(message = "应用类型不能为空")
    @ApiModelProperty(value = "应用类型", dataType = "ApplicationType", required = true)
    private ApplicationType applicationType;

    @NotBlank(message = "支付商户配置名不能为空")
    @ApiModelProperty(value = "支付商户配置名", dataType = "String", required = true)
    private String configName;

    @NotNull(message = "支付类型不能为空")
    @ApiModelProperty(value = "支付类型", dataType = "SourceType", required = true)
    private SourceType sourceType;

    @DecimalMin(value = "1", message = "支付金额必须大于0")
    @ApiModelProperty(value = "支付金额(分)", dataType = "Integer", required = true)
    private Integer fee;

    @NotBlank(message = "支付项目不能为空")
    @ApiModelProperty(value = "支付项目（描述信息）", dataType = "String", required = true)
    private String memo;

    @NotBlank(message = "回调地址不能为空")
    @ApiModelProperty(value = "支付回调地址", dataType = "String", required = true)
    private String backUrl;
}
