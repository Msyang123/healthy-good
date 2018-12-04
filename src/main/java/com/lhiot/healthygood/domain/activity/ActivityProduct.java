package com.lhiot.healthygood.domain.activity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
* Description:活动商品实体类
* @author yangjiawen
* @date 2018/11/24
*/
@Data
@ToString(callSuper = true)
@ApiModel
@NoArgsConstructor
public class ActivityProduct {

    /**
     *
     */
    @JsonProperty("id")
    @ApiModelProperty(value = "主键id", dataType = "Long")
    private Long id;

    /**
    *
    */
    @JsonProperty("activityId")
    @ApiModelProperty(value = "活动id", dataType = "Long")
    @NotNull(message = "活动id不为空")
    private Long activityId;


    @JsonProperty("specialProductActivityId")
    @ApiModelProperty(value = "新品尝鲜活动id", dataType = "Long")
    @NotNull(message = "新品尝鲜活动id不为空")
    private Long specialProductActivityId;
    /**
    *
    */
    @JsonProperty("activityType")
    @ApiModelProperty(value = "活动类型", dataType = "String")
    @NotNull(message = "活动类型不为空")
    private String activityType;

    /**
    *
    */
    @JsonProperty("productShelfId")
    @ApiModelProperty(value = "商品上架id", dataType = "Long")
    @NotNull(message = "商品上架id不为空")
    private Long productShelfId;

    /**
    *
    */
    @JsonProperty("activityPrice")
    @ApiModelProperty(value = "活动价", dataType = "Integer")
    private Integer activityPrice;

    /**
     *  序号
     */
    @JsonProperty("sort")
    @ApiModelProperty(value = "序号", dataType = "Integer")
    private Integer sort;

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

