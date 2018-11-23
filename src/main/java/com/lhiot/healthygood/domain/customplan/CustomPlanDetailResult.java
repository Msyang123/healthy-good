package com.lhiot.healthygood.domain.customplan;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

public class CustomPlanDetailResult {
    /**
     *
     */
    @JsonProperty("id")
    @ApiModelProperty(value = "", dataType = "Long")
    private Long id;

    /**
     *名称
     */
    @JsonProperty("name")
    @ApiModelProperty(value = "名称", dataType = "String")
    private String name;

    /**
     *描述
     */
    @JsonProperty("description")
    @ApiModelProperty(value = "描述", dataType = "String")
    private String description;

    /**
     *banner图片
     */
    @JsonProperty("image")
    @ApiModelProperty(value = "banner图片", dataType = "String")
    private String image;

    /**
     *创建时间
     */
    @JsonProperty("createAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间", dataType = "Date")
    private java.util.Date createAt;
}
