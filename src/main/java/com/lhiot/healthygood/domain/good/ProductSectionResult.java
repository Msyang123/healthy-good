package com.lhiot.healthygood.domain.good;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * @author xiaojian  created in  2018/11/15 16:46
 */
@Data
@ApiModel
public class ProductSectionResult {
    @ApiModelProperty(notes = "主键Id", dataType = "Long", readOnly = true)
    private Long id;
    @ApiModelProperty(notes = "位置ID", dataType = "Long")
    private Long positionId;
    @NotNull(message = "板块名称不能为空")
    @ApiModelProperty(notes = "板块名称", dataType = "String")
    private String sectionName;
    @ApiModelProperty(notes = "板块图片url", dataType = "String")
    private String sectionImg;
    @ApiModelProperty(notes = "排序字段", dataType = "Integer")
    private Integer sorting;
    @ApiModelProperty(notes = "创建时间", dataType = "Date", readOnly = true)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createAt;
    @ApiModelProperty(notes = "父ID", dataType = "Long")
    private Long parentId;

    @ApiModelProperty(notes = "商品上架信息", dataType = "Products", readOnly = true)
    protected List<Products> products;

}
