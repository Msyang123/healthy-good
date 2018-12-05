package com.lhiot.healthygood.feign.model;

import com.lhiot.healthygood.feign.type.AttachmentType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * @author xiaojian created in 2018/11/13 10:51
 **/
@Data
@ApiModel
@EqualsAndHashCode(of = "url")
public class ProductAttachment {
    @ApiModelProperty(notes = "主键Id", dataType = "Long", readOnly = true)
    private Long id;
    @ApiModelProperty(notes = "附件地址", dataType = "String")
    private String url;
    @NotNull(message = "商品ID不能为空")
    @ApiModelProperty(notes = "商品ID", dataType = "Long")
    private Long productId;
    @ApiModelProperty(notes = "排序字段", dataType = "Integer")
    private Integer sorting;
    @ApiModelProperty(notes = "附件类型", dataType = "AttachmentType")
    private AttachmentType attachmentType;
}
