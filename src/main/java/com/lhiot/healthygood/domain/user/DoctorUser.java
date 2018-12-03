package com.lhiot.healthygood.domain.user;

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
* Description:鲜果师客户实体类
* @author yijun
* @date 2018/07/26
*/
@Data
@ToString(callSuper = true)
@ApiModel
@NoArgsConstructor
public class DoctorUser{

    /**
    *id
    */
    @JsonProperty("id")
    @ApiModelProperty(value = "id", dataType = "Long")
    private Long id;

    /**
    *用户备注
    */
    @JsonProperty("remark")
    @ApiModelProperty(value = "用户备注", dataType = "String")
    private String remark;

    /**
    *鲜果师编号
    */
    @JsonProperty("doctorId")
    @ApiModelProperty(value = "鲜果师编号", dataType = "Long")
    private Long doctorId;

    /**
    *用户编号
    */
    @JsonProperty("userId")
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "用户编号", dataType = "Long")
    private Long userId;

    /**
     *微信昵称
     */
    @JsonProperty("nickname")
    @ApiModelProperty(value = "微信昵称", dataType = "String")
    private String nickname;

    /**
     * 昵称首字母
     */
    private String nicknameFristChar;


    /**
     *手机号
     */
    @JsonProperty("phone")
    @ApiModelProperty(value = "手机号", dataType = "String")
    private String phone;


    /**
     *用户头像
     */
    @JsonProperty("avatar")
    @ApiModelProperty(value = "用户头像", dataType = "String")
    private String avatar;

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
