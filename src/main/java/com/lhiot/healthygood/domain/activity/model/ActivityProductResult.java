package com.lhiot.healthygood.domain.activity.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author hufan created in 2018/12/4 12:30
 **/
@ApiModel
@Data
@ToString
@NoArgsConstructor
public class ActivityProductResult {
    @ApiModelProperty(value = "活动商品id", dataType = "Long")
    private Long id;
    @ApiModelProperty(value = "活动id", dataType = "Long")
    private Long activityId;
    @ApiModelProperty(value = "新品尝鲜活动id", dataType = "Long")
    private Long specialProductActivityId;
    @ApiModelProperty(value = "活动类型", dataType = "String")
    private String activityType;
    @JsonProperty("shelfId")
    @ApiModelProperty(value = "商品上架id", dataType = "Long")
    private Long productShelfId;
    @ApiModelProperty(value = "活动价", dataType = "Integer")
    private Integer activityPrice;
    @ApiModelProperty(value = "序号", dataType = "Integer")
    private Integer sort;

    @ApiModelProperty(notes = "上架名称", dataType = "String")
    private String name;
    @ApiModelProperty(notes = "上架图片", dataType = "String")
    private String image;
    @ApiModelProperty(notes = "上架规格", dataType = "String")
    private String specification;
    @ApiModelProperty(notes = "规格条码", dataType = "String")
    private String barcode;
    @ApiModelProperty(notes = "特价", dataType = "Integer")
    private Integer price;
    @ApiModelProperty(notes = "原价", dataType = "Integer")
    private Integer originalPrice;

    @ApiModelProperty(notes = "上架商品信息（排毒养颜套餐  1盒*1份  [00815]）", dataType = "String", readOnly = true)
    private String specificationInfo;
}