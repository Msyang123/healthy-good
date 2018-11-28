package com.lhiot.healthygood.domain.customplan;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author hufan created in 2018/11/26 18:38
 **/
@ApiModel
@Data
public class CustomPlanParam {
    @ApiModelProperty(value = "定制计划ID", dataType = "Long", readOnly = true)
    private Long id;
    @ApiModelProperty(value = "名称", dataType = "String")
    private String name;
    @ApiModelProperty(value = "描述", dataType = "String")
    private String description;
    @ApiModelProperty(value = "banner图片", dataType = "String")
    private String image;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间", dataType = "Date", readOnly = true)
    private Date createAt;
    @ApiModelProperty(value = "到期规则", dataType = "String")
    private String overRule;
    @ApiModelProperty(value = "定制计划状态", dataType = "String")
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