package com.lhiot.healthygood.domain.common;

import com.esotericsoftware.kryo.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
@Data
@ApiModel(description = "门店信息")
public class StoreResult {


    /**
     *storeId
     */
    @JsonProperty("storeId")
    @ApiModelProperty(value = "", dataType = "Long")
    private Long storeId;
    /**
     *名称
     */
    @JsonProperty("storeCode")
    @ApiModelProperty(value = "门店编码", dataType = "String")
    private String storeCode;
    /**
     *门店名称
     */
    @JsonProperty("storeName")
    @ApiModelProperty(value = "门店名称", dataType = "String")
    private String storeName;
    /**
     *距离
     */
    @JsonProperty("distance")
    @ApiModelProperty(value = "距离", dataType = "String")
    private String distance;
}
