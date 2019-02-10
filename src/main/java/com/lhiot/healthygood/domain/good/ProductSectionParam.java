package com.lhiot.healthygood.domain.good;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lhiot.healthygood.type.YesOrNo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ProductSectionParam {
    @ApiModelProperty(notes = "每页查询条数(为空或0不分页查所有)", dataType = "Integer")
    private Integer rows;
    @ApiModelProperty(notes = "当前页", dataType = "Integer")
    private Integer page;

    @ApiModelProperty(hidden = true)
    private Integer startRow;

    @JsonIgnore
    public Integer getStartRow() {
        if (this.rows != null && this.rows > 0) {
            return (this.page != null && this.page > 0 ? this.page - 1 : 0) * this.rows;
        }
        return null;
    }
}
