package com.lhiot.healthygood.feign.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.Objects;

/**
 * @author xiaojian  created in  2018/11/21 18:13
 */
@ApiModel
@Data
public class ArticleSectionParam {
    @ApiModelProperty(notes = "位置ID(多个以英文逗号分隔)", dataType = "String")
    private String positionIds;
    @ApiModelProperty(notes = "父级ID", dataType = "Long")
    private Long parentId;
    @ApiModelProperty(notes = "板块中文名称", dataType = "String")
    private String nameCn;
    @ApiModelProperty(notes = "板块英文名称", dataType = "String")
    private String nameEn;
    @ApiModelProperty(notes = "应用类型", dataType = "String")
    private String applicationType;
    @ApiModelProperty(notes = "起始创建时间", dataType = "Date")
    private Date beginCreateAt;
    @ApiModelProperty(notes = "截止创建时间", dataType = "Date")
    private Date endCreateAt;
    @ApiModelProperty(notes = "版块内文章ID", dataType = "Long")
    private Long articleId;
    @ApiModelProperty(notes = "是否加载版块下文章信息(为空则默认为false)", dataType = "Boolean")
    private Boolean includeArticles;
    @ApiModelProperty(notes = "加载文章最大条数(includeArticles为true起用，为空则加载所有)", dataType = "Long")
    private Long includeArticlesQty;
    @ApiModelProperty(notes = "每页查询条数(为空或0不分页查所有)", dataType = "Integer")
    private Integer rows;
    @ApiModelProperty(notes = "当前页", dataType = "Integer")
    private Integer page;

    @ApiModelProperty(hidden = true)
    private Integer startRow;
    @ApiModelProperty(hidden = true)
    private Boolean pageFlag;

    @JsonIgnore
    public Integer getStartRow() {
        if (Objects.nonNull(this.rows) && this.rows > 0) {
            return (Objects.nonNull(this.page) && this.page > 0 ? this.page - 1 : 0) * this.rows;
        }
        return null;
    }

    @JsonIgnore
    public Boolean getPageFlag() {
        return Objects.nonNull(this.page) && Objects.nonNull(this.rows) && this.page > 0 && this.rows > 0;
    }
}
