package com.lhiot.healthygood.domain.customplan;

import com.lhiot.healthygood.type.CustomOrderBuyType;
import com.lhiot.healthygood.type.CustomOrderStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.Date;

@Data
@ToString(callSuper = true)
@ApiModel(description = "创建定制计划订单对象")
public class CustomOrder {

    @ApiModelProperty(hidden = true)
    private Long id;

    @ApiModelProperty(hidden = true)
    private Long userId;

    @ApiModelProperty(notes = "WAIT_PAYMENT待付款、INVALID已失效、PAUSE_DELIVERY暂停配送、CUSTOMING定制中、FINISHED已结束", dataType = "CustomOrderStatus",hidden = true)
    private CustomOrderStatus status;

    @ApiModelProperty(value = "剩余配送次数",dataType = "Integer",hidden = true)
    private Integer remainingQty;

    @ApiModelProperty(notes = "MANUAL-手动，AUTO-自动", dataType = "CustomOrderBuyType")
    private CustomOrderBuyType deliveryType;

    @ApiModelProperty(value = "配送总次数-周期数",dataType = "Integer",hidden = true)
    private Integer totalQty;

    @ApiModelProperty(value = "定制计划id",dataType = "Integer")
    @Min(value = 1L)
    private Long planId;

    @ApiModelProperty(value = "购买价格",dataType = "Integer",hidden = true)
    private Integer price;

    @ApiModelProperty(value = "创建时间",dataType = "Date",hidden = true)
    private Date createAt;

    @ApiModelProperty(value = "x人套餐",dataType = "Integer",hidden = true)
    private Integer quantity;

    @ApiModelProperty(notes = "定制计划规格id", dataType = "Long")
    @Min(value = 1L)
    private Long specificationId;

    @ApiModelProperty(notes = "客户要求配送时间", dataType = "String")
    @NotBlank(message = "客户要求配送时间不能为空")
    private String deliveryTime;

    @ApiModelProperty(notes = "送货地址", dataType = "String")
    @NotBlank(message = "送货地址不能为空")
    private String deliveryAddress;

    @ApiModelProperty(notes = "门店编码", dataType = "String")
    @NotBlank(message = "门店编码不能为空")
    private String storeCode;
}
