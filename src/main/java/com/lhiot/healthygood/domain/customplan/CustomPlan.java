package com.lhiot.healthygood.domain.customplan;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.lhiot.healthygood.common.PagerRequestObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
* Description:定制计划实体类
* @author zhangs
* @date 2018/11/22
*/
@Data
@ToString(callSuper = true)
@ApiModel
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CustomPlan extends PagerRequestObject {

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
    

    /**
    *到期规则
    */
    @JsonProperty("overRule")
    @ApiModelProperty(value = "到期规则", dataType = "String")
    private String overRule;

    /**
    *VALID INVALID
    */
    @JsonProperty("status")
    @ApiModelProperty(value = "VALID INVALID", dataType = "String")
    private String status;

}
