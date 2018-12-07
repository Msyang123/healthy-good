package com.lhiot.healthygood.feign.model;

import com.lhiot.healthygood.feign.type.ApplicationType;
import com.lhiot.healthygood.feign.type.CoordinateSystem;
import com.lhiot.healthygood.feign.type.DeliverType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * @author yj  created in 11:27 18.9.15
 */
@Data
@ApiModel
@ToString
public class DeliveryParam{

    @ApiModelProperty(notes = "应用类型", required = true, dataType = "ApplicationType")
    private ApplicationType applicationType;

    @ApiModelProperty(notes = "配送回调地址", required = true, dataType = "String")
    private String backUrl;

    @ApiModelProperty(notes = "坐标系）", required = true, dataType = "CoordinateSystem")
    private CoordinateSystem coordinate;

    @ApiModelProperty(notes = "配送时间对象", required = true, dataType = "DeliverTime")
    private DeliverTime deliverTime;

    @ApiModelProperty(notes = "配送类型", dataType = "DeliverType")
    private DeliverType deliveryType;

    @ApiModelProperty(notes = "纬度",dataType = "Double")
    private Double lat;

    @ApiModelProperty(notes = "经度",dataType = "Double")
    private Double lng;

}
