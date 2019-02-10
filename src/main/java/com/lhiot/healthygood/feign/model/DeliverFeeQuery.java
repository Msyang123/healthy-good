package com.lhiot.healthygood.feign.model;

import com.lhiot.healthygood.feign.type.ApplicationType;
import com.lhiot.healthygood.feign.type.CoordinateSystem;
import com.lhiot.healthygood.feign.type.DeliverAtType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author Leon (234239150@qq.com) created in 15:36 18.11.10
 */
@Data
@ApiModel
public class DeliverFeeQuery {

    @ApiModelProperty(notes = "订单费用", dataType = "Integer")
    private Integer orderFee;

    @ApiModelProperty(notes = "订单所在门店code", dataType = "String")
    private String storeCode;

    @ApiModelProperty(notes = "订单所在门店", dataType = "Long")
    private Long storeId;

    @ApiModelProperty(notes = "订单重量(kg)", dataType = "Double")
    private Double weight;

    @ApiModelProperty(notes = "应用类型", dataType = "ApplicationType")
    private ApplicationType applicationType;

    @ApiModelProperty(notes = "订单目标地址", dataType = "String")
    private String address;

    @ApiModelProperty(notes = "订单目标坐标位置-经度", dataType = "Double")
    private Double targetLng;

    @ApiModelProperty(notes = "订单目标坐标位置-纬度", dataType = "Double")
    private Double targetLat;

    @ApiModelProperty(notes = "目标坐标位置使用的坐标系", dataType = "CoordinateSystem")
    private CoordinateSystem coordinateSystem;

    @ApiModelProperty(notes = "配送时间段枚举",dataType = "DeliverAtType")
    private DeliverAtType deliverAtType;

    @ApiModelProperty(notes = "商品列表", dataType = "OrderProduct")
    @NotNull
    @Size(min=1,max = 1000)
    private List<OrderProduct> orderProducts;
}
