package com.lhiot.healthygood.domain.activity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lhiot.healthygood.common.PagerRequestObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
* Description:活动商品实体类
* @author yangjiawen
* @date 2018/11/24
*/
@Data
@ToString(callSuper = true)
@ApiModel
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ActivityProduct extends PagerRequestObject {

    /**
    *
    */
    @JsonProperty("id")
    @ApiModelProperty(value = "", dataType = "Long")
    private Long id;

    /**
    *
    */
    @JsonProperty("activityId")
    @ApiModelProperty(value = "", dataType = "Long")
    private Long activityId;

    /**
    *
    */
    @JsonProperty("activityType")
    @ApiModelProperty(value = "", dataType = "String")
    private String activityType;

    /**
    *
    */
    @JsonProperty("productShelfId")
    @ApiModelProperty(value = "", dataType = "Long")
    private Long productShelfId;

    /**
    *
    */
    @JsonProperty("activityPrice")
    @ApiModelProperty(value = "", dataType = "Integer")
    private Integer activityPrice;

}
