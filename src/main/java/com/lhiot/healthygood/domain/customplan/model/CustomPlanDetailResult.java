package com.lhiot.healthygood.domain.customplan.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lhiot.healthygood.type.ValidOrInvalid;
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
    @ApiModelProperty(value = "定制计划id", dataType = "Long")
    private Long id;
    /**
     *状态
     */
    @JsonProperty("status")
    @ApiModelProperty(value = "状态 VALID-有效, INVALID-无效;", dataType = "String")
    private ValidOrInvalid status;
    /**
     *创建时间
     */
    @JsonProperty("createAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间", dataType = "Date")
    private java.util.Date createAt;

    /**
     *到期规则
     */
    @JsonProperty("overRule")
    @ApiModelProperty(value = "到期规则", dataType = "String")
    private String overRule;

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
     *定制计划中定制规格最低价格
     */
    @JsonProperty("price")
    @ApiModelProperty(value = "定制计划中定制规格最低价格", dataType = "Long")
    private Long price;

    @JsonProperty("periodList")
    @ApiModelProperty(value = "周期类型", dataType = "List")
    private List<CustomPlanPeriodResult> periodList;

    /**
     * 关联的定制板块
     */
    @ApiModelProperty(value = "定制板块ids", dataType = "List")
    private List<Long> customPlanSectionIds;
}
