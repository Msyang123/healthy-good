package com.lhiot.healthygood.feign.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lhiot.healthygood.feign.type.PositionType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xiaojian  created in  2018/11/20 18:14
 */
@ApiModel
@Data
public class UiPositionParam {
    @ApiModelProperty(notes = "类别:PRODUCT-商品，ADVERTISEMENT-广告，ARTICLE-文章", dataType = "PositionType")
    private PositionType positionType;
    @ApiModelProperty(notes = "应用类型", dataType = "String")
    private String applicationType;
    @ApiModelProperty(notes = "位置编码", dataType = "String")
    private String codes;
    @ApiModelProperty(notes = "位置描述", dataType = "String")
    private String description;
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
