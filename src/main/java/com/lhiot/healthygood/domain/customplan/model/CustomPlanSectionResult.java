package com.lhiot.healthygood.domain.customplan.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.leon.microx.web.result.Pages;
import com.lhiot.healthygood.domain.customplan.CustomPlan;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
@ToString(callSuper = true)
@ApiModel
@NoArgsConstructor
public class CustomPlanSectionResult {
    /**
     *
     */
    @JsonProperty("id")
    @ApiModelProperty(value = "定制板块id", dataType = "Long")
    private Long id;
    /**
     *
     */
    @JsonProperty("sectionImage")
    @ApiModelProperty(value = "定制板块图片", dataType = "String")
    private String image;
    /**
     *
     */
    @JsonProperty("sectionName")
    @ApiModelProperty(value = "定制板块名称", dataType = "String")
    private String sectionName;

    /**
     *
     */
    @JsonProperty("sectionCode")
    @ApiModelProperty(value = "定制板块编码", dataType = "String")
    @NotNull
    private String sectionCode;
    /**
     *
     */
    @JsonProperty("createAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间", dataType = "Date", readOnly = true)
    private Date createAt;

    @JsonProperty("customPlanList")
    @ApiModelProperty(value = "定制计划集合", dataType = "Pages")
    @NotNull
    private Pages<CustomPlan> customPlanList;

}
