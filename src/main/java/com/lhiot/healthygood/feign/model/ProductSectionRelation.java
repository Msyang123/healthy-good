package com.lhiot.healthygood.feign.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author xiaojian  created in  2018/11/16 9:13
 */
@Data
@ApiModel
@AllArgsConstructor
public class ProductSectionRelation {
    @ApiModelProperty(notes = "主键Id", dataType = "Long", readOnly = true)
    private Long id;
    @NotNull(message = "商品上架ID不能为空")
    @ApiModelProperty(notes = "商品上架ID", dataType = "Long")
    private Long shelfId;
    @NotNull(message = "商品板块ID不能为空")
    @ApiModelProperty(notes = "商品板块ID", dataType = "Long")
    private Long sectionId;

}
