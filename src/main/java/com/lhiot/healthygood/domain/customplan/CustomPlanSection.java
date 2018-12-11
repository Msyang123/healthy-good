package com.lhiot.healthygood.domain.customplan;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.leon.microx.web.result.Pages;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
* Description:定制计划板块实体类
* @author zhangs
* @date 2018/11/22
*/
@Data
@ToString(callSuper = true)
@ApiModel
@NoArgsConstructor
public class CustomPlanSection{

    /**
    *
    */
    @JsonProperty("id")
    @ApiModelProperty(value = "主键id", dataType = "Long", readOnly = true)
    private Long id;

    /**
    *
    */
    @ApiModelProperty(value = "", dataType = "String")
    private String sectionImage;

    /**
    *
    */
    @JsonProperty("url")
    @ApiModelProperty(value = "", dataType = "String")
    private String url;

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
    @JsonProperty("sort")
    @ApiModelProperty(value = "", dataType = "Integer")
    private Integer sort;

    /**
    *
    */
    @JsonProperty("createAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @ApiModelProperty(value = "", dataType = "Date", readOnly = true)
    private Date createAt;


    @JsonProperty("customPlanList")
    @ApiModelProperty(value = "定制计划集合", dataType = "List")
    @NotNull
    private List<CustomPlan> customPlanList;

}
