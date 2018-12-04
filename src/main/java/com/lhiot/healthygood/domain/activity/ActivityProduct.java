package com.lhiot.healthygood.domain.activity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
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
public class ActivityProduct{

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

    @JsonProperty("productShelfIds")
    @ApiModelProperty(value = "", dataType = "String")
    private String productShelfIds;

    @ApiModelProperty(notes = "每页查询条数(为空或0不分页查所有)", dataType = "Integer")
    private Integer rows;
    @ApiModelProperty(notes = "当前页", dataType = "Integer")
    private Integer page;

    @ApiModelProperty(hidden = true)
    private Integer startRow;

    @JsonIgnore
    public Integer getStartRow() {
        if (this.rows != null && this.rows > 0) {
            return (this.page != null && this.page > 0 ? this.page - 1 : 0) * this.rows;
        }
        return null;
    }
}

