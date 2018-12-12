package com.lhiot.healthygood.feign.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.leon.microx.predefine.OnOff;
import com.lhiot.healthygood.type.ShelfType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import springfox.documentation.annotations.ApiIgnore;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author zhangfeng create in 15:38 2018/11/8
 */
@Data
@ApiModel
public class ProductShelf {
    @ApiModelProperty(notes = "规格对象", dataType = "ProductSpecification")
    private ProductSpecification productSpecification;
    @ApiModelProperty(/*notes = "主键Id", dataType = "Long",*/ readOnly = true, hidden = true)
    private Long id;
    @ApiModelProperty(notes="上架商品ID 对于基础服务 规格对象 主键Id",dataType="Long")
    private Long shelfId;
    @ApiModelProperty(notes = "上架名称", dataType = "String")
    private String name;
    @ApiModelProperty(notes = "规格ID", dataType = "Long")
    private Long specificationId;
    @ApiModelProperty(notes = "上架数量", dataType = "BigDecimal")
    private BigDecimal shelfQty;
    @ApiModelProperty(notes = "特价", dataType = "Integer")
    private Integer price;
    @ApiModelProperty(notes = "原价", dataType = "Integer")
    private Integer originalPrice;
    @ApiModelProperty(notes = "上架图片", dataType = "String")
    private String image;
    @ApiModelProperty(notes = "商品主图", dataType = "String")
    private String productImage;
    @ApiModelProperty(notes = "上架状态：ON-上架，OFF-下架", dataType = "OnOff")
    private OnOff shelfStatus;
    @ApiModelProperty(notes = "上架类型：NORMAL-普通商品,GIFT-赠品", dataType = "ShelfType")
    private ShelfType shelfType;
    @ApiModelProperty(notes = "创建时间", dataType = "Date", readOnly = true)
    private Date createAt;
    @ApiModelProperty(notes = "描述", dataType = "String")
    private String description;
    @ApiModelProperty(notes = "排序字段", dataType = "Integer")
    private Integer sorting;
    @ApiModelProperty(notes = "应用类型", dataType = "String")
    private String applicationType;
    @ApiModelProperty(notes = "活动价格", dataType = "Integer")
    private Integer activityPrice;
    @ApiModelProperty(notes = "限制购买的份数", dataType = "Integer")
    private Integer limitCount;
    @ApiModelProperty(notes = "已经购买的份数", dataType = "Integer")
    private Integer alreadyBuyAmount;

    public void setId(Long id) {
        this.id = id;
        this.shelfId = id;
    }
}
