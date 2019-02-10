package com.lhiot.healthygood.feign.model;

import com.lhiot.healthygood.feign.type.ApplicationType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author zhangfeng create in 11:56 2018/11/9
 */
@ApiModel
@Data
public class StoreSearchParam {
    @ApiModelProperty(notes = "门店Id集合",dataType = "String")
    private String storeIds;
    private String name;
    private String code;

    private ApplicationType applicationType;
    private Double distance;
    @ApiModelProperty(notes = "纬度",dataType = "Double")
    private Double lat;
    @ApiModelProperty(notes = "经度",dataType = "Double")
    private Double lng;
    @ApiModelProperty(notes = "查询条数",dataType = "Integer")
    private Integer rows;
    @ApiModelProperty(notes = "当前页",dataType = "Integer")
    private Integer page;

}
