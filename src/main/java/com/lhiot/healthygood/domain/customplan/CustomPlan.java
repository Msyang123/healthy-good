package com.lhiot.healthygood.domain.customplan;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lhiot.healthygood.type.ValidOrInvalid;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
* Description:定制计划实体类
* @author zhangs
* @date 2018/11/22
*/
@Data
@ApiModel
public class CustomPlan{

    /**
    *
    */
    @JsonProperty("id")
    @ApiModelProperty(value = "主键Id", dataType = "Long")
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
    @ApiModelProperty(value = "创建时间", dataType = "Date", readOnly = true)
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
    private ValidOrInvalid status;

    /**
     *创建人
     */
    @JsonProperty("createUser")
    @ApiModelProperty(value = "创建人", dataType = "String")
    private String createUser;

    /**
     * 定制最低价格
     */
    @ApiModelProperty(value = "定制最低价格", dataType = "Long")
    private Long price;

    /**
     *定制计划排序
     */
    @JsonProperty("sort")
    @ApiModelProperty(value = "定制计划排序", dataType = "Long")
    private Long sort;
}
