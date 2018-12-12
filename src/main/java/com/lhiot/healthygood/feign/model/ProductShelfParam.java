package com.lhiot.healthygood.feign.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.leon.microx.predefine.OnOff;
import com.lhiot.healthygood.type.ShelfType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author xiaojian  created in  2018/11/16 11:20
 */
@ApiModel
@Data
public class ProductShelfParam {
    @ApiModelProperty(notes = "版块ID", dataType = "Long")
    private Long sectionId;
    @ApiModelProperty(notes = "上架名称", dataType = "String")
    private String name;
    @ApiModelProperty(notes = "上架状态：ON-上架，OFF-下架", dataType = "OnOff")
    private OnOff shelfStatus;
    @ApiModelProperty(notes = "上架类型：NORMAL-普通商品,GIFT-赠品", dataType = "ShelfType")
    private ShelfType shelfType;
    @ApiModelProperty(notes = "应用类型", dataType = "String")
    private String applicationType;
    @ApiModelProperty(notes = "最小特价", dataType = "Integer")
    private Integer minPrice;
    @ApiModelProperty(notes = "最大特价", dataType = "Integer")
    private Integer maxPrice;
    @ApiModelProperty(notes = "最小原价", dataType = "Integer")
    private Integer minOriginalPrice;
    @ApiModelProperty(notes = "最大原价", dataType = "Integer")
    private Integer maxOriginalPrice;
    @ApiModelProperty(notes = "起始创建时间", dataType = "Date")
    private Date beginCreateAt;
    @ApiModelProperty(notes = "截止创建时间", dataType = "Date")
    private Date endCreateAt;
    @ApiModelProperty(notes = "上架id多个以英文逗号分隔", dataType = "String")
    private String ids;
    @ApiModelProperty(notes = "名称或条码关键字", dataType = "String")
    private String keyword;
    @ApiModelProperty(notes = "是否加载商品信息(为空则默认为false)", dataType = "Boolean", readOnly = true)
    private Boolean includeProduct;
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
