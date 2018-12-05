package com.lhiot.healthygood.feign.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.sql.Timestamp;

/**
* 描述：常见问题
* @author yijun
* @date 2018-07-21
*/
@Data
@ToString
@ApiModel
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class FaqParam implements Serializable {

	@JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(notes = "", dataType = "Long")
    private Long id;

    @ApiModelProperty(notes = "标题", dataType = "String")
    private String title;

    @ApiModelProperty(notes = "内容", dataType = "String")
    private String content;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(notes = "常见问题分类id", dataType = "Long")
    private Long faqCategoryId;

    @ApiModelProperty(notes = "父分类id", dataType = "Long")
    private Long parentId;
    
    @ApiModelProperty(notes = "序号", dataType = "Integer")
    private Integer rankNo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @ApiModelProperty(notes = "创建时间", dataType = "Date")
    private Timestamp createAt;
    
    @ApiModelProperty(notes = "创建人", dataType = "String")
    private String createUser;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(notes = "应用类型", dataType = "String")
    private String applicationType;
    
    @ApiModelProperty(notes = "分类英文名称", dataType = "String")
    private String categoryEnName;

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
