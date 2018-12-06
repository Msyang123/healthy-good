package com.lhiot.healthygood.domain.good;

import com.leon.microx.predefine.OnOff;
import com.lhiot.healthygood.type.ShelfType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel
public class ProductDetailResult {
    @ApiModelProperty(notes = "上架ID", dataType = "Long")
    private Long shelfId;

    @ApiModelProperty(notes = "商品价格", dataType = "Integer")
    private Integer price;

    @ApiModelProperty(notes = "商品主图", dataType = "String")
    private String productImage;

    @ApiModelProperty(notes = "附件图（长方形）", dataType = "String")
    private String subImage;

    @ApiModelProperty(notes = "详情图", dataType = "String")
    private String detail;

    @ApiModelProperty(notes = "上架数量", dataType = "String")
    private BigDecimal shelfQty;

    @ApiModelProperty(notes = "上架类型：NORMAL-普通商品,GIFT-赠品", dataType = "ShelfType")
    private ShelfType shelfType;

    @ApiModelProperty(notes = "上架商品名称", dataType = "String")
    private String name;

    @ApiModelProperty(notes = "上架状态：ON-上架，OFF-下架", dataType = "OnOff")
    private OnOff shelfStatus;

    @ApiModelProperty(notes = "商品描述", dataType = "String")
    private String description;

    @ApiModelProperty(notes = "活动价格", dataType = "String")
    private Integer activityPrice;

    @ApiModelProperty(notes = "限购数量", dataType = "String")
    private Integer limitCount;

    @ApiModelProperty(notes = "已购数量", dataType = "String")
    private Integer alreadyBuyAmount;
}
