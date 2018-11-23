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
* Description:定制计划板块实体类
* @author zhangs
* @date 2018/11/22
*/
@Data
@ToString(callSuper = true)
@ApiModel
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CustomPlanSection extends PagerRequestObject {

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
    @ApiModelProperty(value = "", dataType = "Date")
    private java.util.Date createAt;
    

}
