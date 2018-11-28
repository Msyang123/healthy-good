package com.lhiot.healthygood.domain.doctor;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lhiot.healthygood.common.PagerRequestObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
* Description:鲜果师微信用户实体类
* @author yijun
* @date 2018/07/26
*/
@Data
@ToString(callSuper = true)
@ApiModel
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FruitDoctorUser extends PagerRequestObject {

    /**
    *
    */
    @JsonProperty("id")
    @ApiModelProperty(value = "", dataType = "Long")
    private Long id;

    /**
    *open_id
    */
    @JsonProperty("openId")
    @ApiModelProperty(value = "open_id", dataType = "String")
    private String openId;

    /**
    *性别 MALE男 FEMALE女 UNKNOWN未知
    */
    @JsonProperty("sex")
    @ApiModelProperty(value = "性别 MALE男 FEMALE女 UNKNOWN未知", dataType = "String")
    private String gender;

    /**
    *手机号
    */
    @JsonProperty("phone")
    @ApiModelProperty(value = "手机号", dataType = "String")
    private String phone;

    /**
    *昵称
    */
    @JsonProperty("nickname")
    @ApiModelProperty(value = "昵称", dataType = "String")
    private String nickname;

    /**
    *城市
    */
    @JsonProperty("city")
    @ApiModelProperty(value = "城市", dataType = "String")
    private String city;

    /**
    *国家
    */
    @JsonProperty("country")
    @ApiModelProperty(value = "国家", dataType = "String")
    private String country;

    /**
    *用户语言
    */
    @JsonProperty("language")
    @ApiModelProperty(value = "用户语言", dataType = "String")
    private String language;

    /**
    *注册时间
    */
    @JsonProperty("registerTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @ApiModelProperty(value = "注册时间", dataType = "Date")
    private java.util.Date registrationTime;
    

    /**
    *基础用户表id
    */
    @JsonProperty("baseuserId")
    @ApiModelProperty(value = "基础用户表id", dataType = "Long")
    private Long baseuserId;
    
    /**
    *头像
    */
    @JsonProperty("avatar")
    @ApiModelProperty(value = "头像", dataType = "String")
    private String avatar;

    /**
    *union_id
    */
    @JsonProperty("unionId")
    @ApiModelProperty(value = "union_id", dataType = "String")
    private String unionId;

    /**
    *创建时间
    */
    @JsonProperty("createTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间", dataType = "Date")
    private java.util.Date createTime;


    /**
     *手机验证码
     */
    @JsonProperty("verificationCode")
    @ApiModelProperty(value = "verification_code", dataType = "String")
    private String verificationCode;


    /**
     *鲜果币
     */
    @JsonProperty("currency")
    @ApiModelProperty(value = "currency", dataType = "int")
    private int currency;


    /**
     *是否为鲜果师(是-true，否-false)
     */
    @JsonProperty("fruitDoctor")
    @ApiModelProperty(value = "fruitDoctor", dataType = "boolean")
    private boolean fruitDoctor;

    @ApiModelProperty(value = "用户的备注信息", dataType = "String")
    private String remark;
    
    @JsonIgnore
    @ApiModelProperty(value = "鲜果师id", dataType = "Long")
    private Long doctorId;

    /**
     *ids 查询条件 不返回结果
     */
    @JsonIgnore
    @ApiModelProperty(value = "ids", dataType = "List")
    private List<String> ids;

    /**
     *创建开始时间
     */
    @JsonProperty("createTimeBegin")
    @ApiModelProperty(value = "创建开始时间", dataType = "String")
    private String createTimeBegin;

    /**
     *创建结束时间
     */
    @JsonProperty("createTimeEnd")
    @ApiModelProperty(value = "创建结束时间", dataType = "String")
    private String createTimeEnd;
}
