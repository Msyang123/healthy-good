package com.lhiot.healthygood.domain.customplan.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lhiot.healthygood.type.ValidOrInvalid;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author hufan created in 2018/11/26 18:38
 **/
@Data
@ToString(callSuper = true)
@ApiModel
@NoArgsConstructor
public class CustomPlanParam {
    /**
     * 定制计划
     */
    @ApiModelProperty(value = "主键Id", dataType = "Long", readOnly = true)
    private Long id;
    @ApiModelProperty(value = "名称", dataType = "String")
    @NotNull
    private String name;
    @ApiModelProperty(value = "描述", dataType = "String")
    private String description;
    @ApiModelProperty(value = "banner图片", dataType = "String")
    private String image;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间", dataType = "Date", readOnly = true)
    private java.util.Date createAt;
    @ApiModelProperty(value = "到期规则", dataType = "String")
    private String overRule;
    @ApiModelProperty(value = "定制计划状态 有效-VALID 无效-INVALID", dataType = "ValidOrInvalid")
    private ValidOrInvalid status;

    /**
     * 定制计划和定制板块关联
     */
    @ApiModelProperty(value = "定制板块关联定制计划排序，英文逗号分隔", dataType = "String")
    @NotNull
    private String sorts;
    @ApiModelProperty(value = "定制板块ids，英文逗号分隔", dataType = "String")
    @NotNull
    private String customPlanSectionIds;

    /**
     * 定制规格
     */
    @ApiModelProperty(value = "定制计划规格集合", dataType = "CustomPlanSpecification")
    @NotNull
    private List<CustomPlanSpecificationResult> customPlanSpecifications;

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