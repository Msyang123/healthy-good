package com.lhiot.healthygood.domain.doctor;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.lhiot.healthygood.type.AuditStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

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
    @ApiModelProperty(value = "审核状态（UNAUDITED待审核  AGREE审核通过  REJECT审核不通过）", dataType = "AuditStatus")
    private AuditStatus auditStatus;

    /**
    *申请时间
    */
    @JsonProperty("createTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
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
     *审核人
     */
    @JsonProperty("auditUser")
    @ApiModelProperty(value = "审核人", dataType = "String")
    private String auditUser;

    /**
    *用户id
    */
    @JsonProperty("userId")
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "用户id", dataType = "Long")
    private Long userId;

    /**
     *起始创建时间
     */
    @JsonProperty("beginCreateAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @ApiModelProperty(notes = "起始创建时间", dataType = "Date")
    private Date beginCreateAt;

    /**
     *截止创建时间
     */
    @JsonProperty("endCreateAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @ApiModelProperty(notes = "截止创建时间", dataType = "Date")
    private Date endCreateAt;

    @ApiModelProperty(notes = "每页查询条数(为空或0不分页查所有)", dataType = "Integer")
    private Integer rows;
    @ApiModelProperty(notes = "当前页", dataType = "Integer")
    private Integer page;
    @ApiModelProperty(hidden = true)
    private Integer startRow;
    @JsonIgnore
    public Integer getStartRow() {
        if (this.rows != null && this.rows > 0) {
            return (this.page != null && this.page > 0 ? this.page - 1 : 0) * this.rows;
        }
        return null;
    }

}
