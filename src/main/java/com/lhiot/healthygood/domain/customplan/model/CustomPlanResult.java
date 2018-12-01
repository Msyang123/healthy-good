package com.lhiot.healthygood.domain.customplan.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author hufan created in 2018/11/29 9:45
 **/
@ApiModel
@Data
public class CustomPlanResult {
    /**
     * 定制计划
     */
    @ApiModelProperty(value = "主键Id", dataType = "Long", readOnly = true)
    private Long id;
    @ApiModelProperty(value = "定制计划名称", dataType = "String")
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
    @NotNull
    private String overRule;
    @ApiModelProperty(value = "定制计划状态 有效-VALID 无效-INVALID", dataType = "String")
    @NotNull
    private String status;

    /**
     * 定制计划和定制板块关联
     */
    @ApiModelProperty(value = "定制板块关联定制计划排序", dataType = "String")
    @NotNull
    private String sorts;
    @ApiModelProperty(value = "定制板块ids", dataType = "String")
    @NotNull
    private String customPlanSectionIds;

    /**
     * 定制规格
     */
    @ApiModelProperty(value = "定制计划规格", dataType = "CustomPlanSpecification")
    @NotNull
    private List<CustomPlanSpecificationResult> customPlanSpecifications;

}