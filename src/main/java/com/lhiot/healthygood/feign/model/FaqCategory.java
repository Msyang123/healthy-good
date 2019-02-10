package com.lhiot.healthygood.feign.model;

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
import java.util.List;

/**
* 描述：faq分类表
* @author yijun
* @date 2018-07-21
*/
@Data
@ToString
@ApiModel
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class FaqCategory implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(notes = "", dataType = "Long")
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(notes = "父级id", dataType = "Long")
    private Long parentId;

    @ApiModelProperty(notes = "分类名称", dataType = "String")
    private String categoryName;
    
    @ApiModelProperty(notes = "分类英文名称", dataType = "String")
    private String categoryEnName;
    
    @ApiModelProperty(notes = "faq", dataType = "java.util.List")
    private List<Faq> faqs;


    @ApiModelProperty(notes = "每页查询条数(为空或0不分页查所有)", dataType = "Integer")
    private Integer rows;

    @JsonIgnore
    @ApiModelProperty(notes = "当前页", dataType = "Integer")
    private Integer page;

    @JsonIgnore
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
