package com.lhiot.healthygood.domain.customplan.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lhiot.healthygood.domain.customplan.CustomPlan;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author hufan created in 2018/11/26 18:38
 **/
@ApiModel
@Data
public class CustomPlanSectionParam {
    @ApiModelProperty(value = "主键id", dataType = "Long", readOnly = true)
    private Long id;
    @ApiModelProperty(value = "定制板块图片", dataType = "String")
    private String sectionImage;
    @ApiModelProperty(value = "定制板块url", dataType = "String")
    private String url;
    @ApiModelProperty(value = "定制板块名称", dataType = "String")
    private String sectionName;
    @ApiModelProperty(value = "定制板块编码", dataType = "String")
    private String sectionCode;
    @ApiModelProperty(value = "排序", dataType = "Integer")
    private Integer sort;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间", dataType = "Date", readOnly = true)
    private Date createAt;

    @ApiModelProperty(value = "定制计划列表", dataType = "CustomPlan",readOnly = true)
    private List<CustomPlan> customPlanList;

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