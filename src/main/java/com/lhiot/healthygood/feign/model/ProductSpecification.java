package com.lhiot.healthygood.feign.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.leon.microx.predefine.Use;
import com.lhiot.healthygood.feign.type.InventorySpecification;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author zhangfeng create in 9:11 2018/11/9
 */
@Data
@ApiModel
public class ProductSpecification {
    @ApiModelProperty(notes = "主键Id", dataType = "Long", readOnly = true)
    private Long id;
    @NotNull(message = "商品ID不能为空")
    @ApiModelProperty(notes = "商品ID", dataType = "Long")
    private Long productId;
    @ApiModelProperty(notes = "商品对象", dataType = "Product", readOnly = true)
    private Product product;
    @ApiModelProperty(notes = "商品条码", dataType = "String")
    private String barcode;
    @ApiModelProperty(notes = "打包单位", dataType = "String")
    private String packagingUnit;
    @ApiModelProperty(notes = "单份规格商品的重量", dataType = "BigDecimal")
    private BigDecimal weight;
    @ApiModelProperty(notes = "海鼎规格数量", dataType = "BigDecimal")
    private BigDecimal specificationQty;
    @ApiModelProperty(notes = "安全库存", dataType = "Integer")
    private Integer limitInventory;
    @ApiModelProperty(notes = "是否为库存规格：YES-是，NO-否", dataType = "InventorySpecification")
    private InventorySpecification inventorySpecification;
    @ApiModelProperty(notes = "是否可用：ENABLE-可用，DISABLE-不可用", dataType = "Use")
    private Use availableStatus;
    @ApiModelProperty(notes = "创建时间", dataType = "Date", readOnly = true)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createAt;
}
