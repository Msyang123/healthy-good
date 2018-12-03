package com.lhiot.healthygood.domain.doctor;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
* Description:鲜果师申请记录实体类
* @author yijun
* @date 2018/07/26
*/
@Data
@ToString(callSuper = true)
@ApiModel
@NoArgsConstructor
public class RegisterApplication{

    /**
    *id
    */
    @JsonProperty("id")
    @ApiModelProperty(value = "id", dataType = "Long")
    private Long id;

    /**
    *身份证号
    */
    @JsonProperty("idcardNo")
    @ApiModelProperty(value = "身份证号", dataType = "String")
    private String idcardNo;

    /**
    *手机号码
    */
    @JsonProperty("phone")
    @ApiModelProperty(value = "手机号码", dataType = "String")
    private String phone;

    /**
     *验证码
     */
    @JsonProperty("verificationCode")
    @ApiModelProperty(value = "验证码", dataType = "String")
    private String verificationCode;

    /**
    *身份证正面
    */
    @JsonProperty("idcardFront")
    @ApiModelProperty(value = "身份证正面", dataType = "String")
    private String idcardFront;

    /**
    *身份证反面
    */
    @JsonProperty("idcardReverse")
    @ApiModelProperty(value = "身份证反面", dataType = "String")
    private String idcardReverse;

    /**
    *资质证明
    */
    @JsonProperty("credentials")
    @ApiModelProperty(value = "资质证明", dataType = "String")
    private String credentials;

    /**
    *申请失败原因
    */
    @JsonProperty("failureReason")
    @ApiModelProperty(value = "申请失败原因", dataType = "String")
    private String failureReason;

    /**
    *审核状态（UNAUDITED待审核  AGREE审核通过  REJECT审核不通过）
    */
    @JsonProperty("auditStatus")
    @ApiModelProperty(value = "审核状态（UNAUDITED待审核  AGREE审核通过  REJECT审核不通过）", dataType = "String")
    private String auditStatus;

    /**
    *申请时间
    */
    @JsonProperty("createTime")
    @ApiModelProperty(value = "申请时间", dataType = "Date")
    private java.util.Date createAt;
    

    /**
    *姓名
    */
    @JsonProperty("realName")
    @ApiModelProperty(value = "姓名", dataType = "String")
    private String realName;

    /**
    *审核时间
    */
    @JsonProperty("auditTime")
    @ApiModelProperty(value = "审核时间", dataType = "Date")
    private java.util.Date auditAt;
    

    /**
    *用户id
    */
    @JsonProperty("userId")
    @ApiModelProperty(value = "用户id", dataType = "Long")
    private Long userId;

}
