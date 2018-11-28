package com.lhiot.healthygood.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
* Description:门店位置实体类
* @author yijun
* @date 2018/09/11
*/
@Data
@ApiModel
public class StorePosition {

    /**
    *
    */
    @JsonProperty("id")
    @ApiModelProperty(value = "", dataType = "Long")
    private Long id;

    /**
    *坐标位置（经度）
    */
    @JsonProperty("lng")
    @ApiModelProperty(value = "坐标位置（经度）", dataType = "Double")
    private Double lng;

    /**
    *坐标位置（纬度）
    */
    @JsonProperty("lat")
    @ApiModelProperty(value = "坐标位置（纬度）", dataType = "Double")
    private Double lat;

    /**
    *应用类型
    */
    @JsonProperty("applicationType")
    @ApiModelProperty(value = "应用类型", dataType = "ApplicationTypeEnum")
    private ApplicationType applicationType;

    /**
    *门店编号
    */
    @JsonProperty("storeId")
    @ApiModelProperty(value = "门店编号", dataType = "Long")
    private Long storeId;

    private List<Long> storeIds;

    @JsonIgnore
    @ApiModelProperty(value = "当前页,默认值1")
    private Long page = 1L;

    /**
     * 传入-1可不分页
     */
    @JsonIgnore
    @ApiModelProperty(value = "每页显示条数,默认值10")
    private Long rows = 10L;

}
