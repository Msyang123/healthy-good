package com.lhiot.healthygood.feign.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author xiaojian  created in  2018/11/17 14:33
 */
@ApiModel
@Data
public class ProductSectionParam {
    @ApiModelProperty(notes = "位置ID", dataType = "Long")
    private Long positionId;
    @ApiModelProperty(notes = "父级ID", dataType = "Long")
    private Long parentId;
    @ApiModelProperty(notes = "板块名称", dataType = "String")
    private String sectionName;
    @ApiModelProperty(notes = "起始创建时间", dataType = "Date")
    private Date beginCreateAt;
    @ApiModelProperty(notes = "截止创建时间", dataType = "Date")
    private Date endCreateAt;
    @ApiModelProperty(notes = "是否加载版块下商品上架信息(为空则默认为false)", dataType = "Boolean")
    private Boolean includeShelves;
    @ApiModelProperty(notes = "加载商品上架最大条数(includeShelves为true起用，为空则加载所有)", dataType = "Long")
    private Long includeShelvesQty;
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
