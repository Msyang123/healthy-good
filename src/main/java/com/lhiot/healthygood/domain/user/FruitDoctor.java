package com.lhiot.healthygood.domain.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
* Description:鲜果师成员实体类
* @author yijun
* @date 2018/07/26
*/
@Data
@ToString(callSuper = true)
@ApiModel
@NoArgsConstructor
public class FruitDoctor{

    /**
    *id
    */
    @JsonProperty("id")
    @ApiModelProperty(value = "id", dataType = "Long")
    private Long id;

    /**
    *鲜果师姓名
    */
    @JsonProperty("realName")
    @ApiModelProperty(value = "鲜果师姓名", dataType = "String")
    private String realName;

    /**
    *鲜果师职业照
    */
    @JsonProperty("photo")
    @ApiModelProperty(value = "鲜果师职业照", dataType = "String")
    private String photo;

    /**
    *鲜果师邀请码
    */
    @JsonProperty("inviteCode")
    @ApiModelProperty(value = "鲜果师邀请码", dataType = "String")
    private String inviteCode;

    /**
    *鲜果师关联用户编号
    */
    @JsonProperty("userId")
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "鲜果师关联用户编号", dataType = "Long")
    private Long userId;

    /**
    *培训中TRAINING、初级PRIMARY 中高级 SENIOR
    */
    @JsonProperty("doctorLevel")
    @ApiModelProperty(value = "培训中TRAINING、初级PRIMARY 中高级 SENIOR", dataType = "String")
    private String doctorLevel;


    /**
     *培训中TRAINING、初级PRIMARY 中高级 SENIOR
     */
    @JsonProperty("doctorLevels")
    @ApiModelProperty(value = "培训中TRAINING、初级PRIMARY 中高级 SENIOR", dataType = "String[]")
    private String[] doctorLevels;


    /**
    *鲜果师状态 VALID正常  INVALID已停用
    */
    @JsonProperty("doctorStatus")
    @ApiModelProperty(value = "鲜果师状态 VALID正常  INVALID已停用", dataType = "String")
    private String doctorStatus;

    /**
    *鲜果师头衔
    */
    @JsonProperty("jobTitle")
    @ApiModelProperty(value = "鲜果师头衔", dataType = "String")
    private String jobTitle;

    /**
    *注册时间
    */
    @JsonProperty("createTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @ApiModelProperty(value = "注册时间", dataType = "Date")
    private java.util.Date createAt;
    

    /**
    *推荐人（鲜果师编号）
    */
    @JsonProperty("refereeId")
    @ApiModelProperty(value = "推荐人（鲜果师编号）", dataType = "Long")
    private Long refereeId;

    /**
    *个人简介
    */
    @JsonProperty("profile")
    @ApiModelProperty(value = "个人简介", dataType = "String")
    private String profile;

    /**
     *银行卡卡号
     */
    @JsonProperty("cardNo")
    @ApiModelProperty(value = "银行卡卡号", dataType = "String")
    private String cardNo;

    /**
     *开户行
     */
    @JsonProperty("bankDeposit")
    @ApiModelProperty(value = "开户行", dataType = "String")
    private String bankDeposit;

    /**
     *持卡人姓名
     */
    @JsonProperty("cardUsername")
    @ApiModelProperty(value = "持卡人姓名", dataType = "String")
    private String cardUsername;

    /**
    *明星鲜果师 NO普通鲜果师YES明星鲜果师
    */
    @JsonProperty("hot")
    @ApiModelProperty(value = "明星鲜果师 NO普通鲜果师YES明星鲜果师", dataType = "String")
    private String hot;

    /**
    *剩余可结算金额
    */
    @JsonProperty("balance")
    @ApiModelProperty(value = "剩余可结算金额", dataType = "Integer")
    private Integer balance;

    /**
     *当月销售额
     */
    @JsonProperty("amountOfMonth")
    @ApiModelProperty(value = "当月销售额", dataType = "Integer")
    private Integer amountOfMonth;

    /**
    *头像
    */
    @JsonProperty("avatar")
    @ApiModelProperty(value = "头像", dataType = "String")
    private String avatar;

    /**
    *鲜果师半身照
    */
    @JsonProperty("upperbodyPhoto")
    @ApiModelProperty(value = "鲜果师半身照", dataType = "String")
    private String upperbodyPhoto;

    /**
    *审核通过的手机号
    */
    @JsonProperty("phone")
    @ApiModelProperty(value = "审核通过的手机号", dataType = "String")
    private String phone;

    /**
    *审核通过记录id
    */
    @JsonProperty("applicationId")
    @ApiModelProperty(value = "审核通过记录id", dataType = "Long")
    private Long applicationId;

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
