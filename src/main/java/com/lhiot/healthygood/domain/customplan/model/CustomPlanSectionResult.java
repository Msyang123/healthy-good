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
    @ApiModelProperty(value = "", dataType = "Long")
    private Long id;
    /**
     *
     */
    @JsonProperty("sectionImage")
    @ApiModelProperty(value = "", dataType = "String")
    private String image;
    /**
     *
     */
    @JsonProperty("sectionName")
    @ApiModelProperty(value = "", dataType = "String")
    private String sectionName;

    /**
     *
     */
    @JsonProperty("sectionCode")
    @ApiModelProperty(value = "", dataType = "String")
    private String sectionCode;
    /**
     *
     */
    @JsonProperty("createAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @ApiModelProperty(value = "", dataType = "Date")
    private java.util.Date createAt;

    @JsonProperty("customPlanList")
    @ApiModelProperty(value = "", dataType = "Pages")
    private Pages<CustomPlan> customPlanList;
}
