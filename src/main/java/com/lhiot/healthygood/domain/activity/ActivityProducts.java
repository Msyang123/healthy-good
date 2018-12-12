package com.lhiot.healthygood.domain.activity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lhiot.healthygood.type.ShelfType;
import com.lhiot.healthygood.type.ValidOrInvalid;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@ApiModel(description = "新品尝鲜活动商品")
@NoArgsConstructor
public class ActivityProducts {

    @ApiModelProperty(notes="活动商品ID",dataType="Long")
    private Long id;

    @ApiModelProperty(notes="上架商品ID",dataType="Long")
    private Long shelfId;

    @ApiModelProperty(notes="活动价格",dataType="Integer")
    private Integer activityPrice;

    @ApiModelProperty(notes="已购买数量",dataType="Integer")
    private Integer alreadyBuyCount;

    @JsonProperty("name")
    @ApiModelProperty(notes="商品名称",dataType="String")
    private String productName;

    @ApiModelProperty(notes="当前状态（有效- VALID 无效-INVALID）",dataType="ValidOrInvalid")
    private ValidOrInvalid status;

    @ApiModelProperty(notes="活动描述",dataType="String")
    private String description;

    @ApiModelProperty(notes="商品价格",dataType="Integer")
    private Integer price;

    @ApiModelProperty(notes="上架图片",dataType="String")
    private String image;

    @ApiModelProperty(notes="上架数量",dataType="BigDecimal")
    private BigDecimal shelfQty;

    @ApiModelProperty(notes="NORMAL-普通商品,GIFT-赠品",dataType="String")
    private ShelfType shelfType;

    @ApiModelProperty(notes="商品主图",dataType="String")
    private String productImage;

    @ApiModelProperty(notes="商品详情图",dataType="Integer")
    private String detail;
}
