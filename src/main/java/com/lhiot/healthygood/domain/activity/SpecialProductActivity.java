package com.lhiot.healthygood.domain.activity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;



/**
* Description:新品尝鲜活动实体类
* @author yangjiawen
* @date 2018/11/24
*/
@Data
@ToString(callSuper = true)
@ApiModel
@NoArgsConstructor
public class SpecialProductActivity{

    /**
    *id
    */
    @JsonProperty("id")
    @ApiModelProperty(value = "id", dataType = "Long")
    private Long id;

    /**
     *活动id
     */
    @ApiModelProperty(notes="活动Id",dataType="Long")
    private Long activityId;

    /**
    *活动名称
    */
    @JsonProperty("activityName")
    @ApiModelProperty(value = "活动名称", dataType = "String")
    private String activityName;

    /**
    *活动描述
    */
    @JsonProperty("description")
    @ApiModelProperty(value = "活动描述", dataType = "String")
    private String description;

    /**
    *每人限制数量
    */
    @JsonProperty("limitCount")
    @ApiModelProperty(value = "每人限制数量", dataType = "Integer")
    private Integer limitCount;

    /**
    *应用类型
    */
    @JsonProperty("applicationType")
    @ApiModelProperty(value = "应用类型", dataType = "String")
    private String applicationType;

    /**
    *创建时间
    */
    @JsonProperty("createAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间", dataType = "Date")
    private java.util.Date createAt;
    

    /**
    *开启状态(VALID 开启 INVALID未开启)
    */
    @JsonProperty("status")
    @ApiModelProperty(value = "开启状态(VALID 开启 INVALID未开启)", dataType = "String")
    private String status;

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
