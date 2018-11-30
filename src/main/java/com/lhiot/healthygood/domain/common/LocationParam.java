package com.lhiot.healthygood.domain.common;

import com.esotericsoftware.kryo.NotNull;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode
@Data
@ApiModel(description = "地理位置信息")
@NoArgsConstructor
public class LocationParam  implements LocationDistance {

    @ApiModelProperty(value = "坐标位置（纬度）", dataType = "Double", required = true)
    @NotNull
    private Double lat;

    @ApiModelProperty(value = "坐标位置（经度）", dataType = "Double", required = true)
    @NotNull
    private Double lng;

}