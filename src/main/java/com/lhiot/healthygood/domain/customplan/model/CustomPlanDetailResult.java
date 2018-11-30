package com.lhiot.healthygood.domain.customplan.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("status")
    @ApiModelProperty(value = "状态", dataType = "String")
    private String status;
    /**
     *创建时间
     */
    @JsonProperty("createAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间", dataType = "Date")
    private java.util.Date createAt;

    /**
     *名称
     */
    @JsonProperty("createUser")
    @ApiModelProperty(value = "创建人", dataType = "String")
    private String createUser;
    /**
     *banner图片
     */
    @JsonProperty("image")
    @ApiModelProperty(value = "banner图片", dataType = "String")
    private String image;
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
     *描述
     */
    @JsonProperty("description")
    @ApiModelProperty(value = "描述", dataType = "Long")
    private Long price;

    @JsonProperty("standardList")
    @ApiModelProperty(value = "", dataType = "List")
    private List<CustomPlanDatailStandardResult> standardList;
}
