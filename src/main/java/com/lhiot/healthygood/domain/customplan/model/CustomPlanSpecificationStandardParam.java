package com.lhiot.healthygood.domain.customplan.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author hufan created in 2018/12/8 9:57
 **/
@Data
@ToString(callSuper = true)
@ApiModel
@NoArgsConstructor
public class CustomPlanSpecificationStandardParam {
    /**
     *
     */
    @JsonProperty("id")
    @ApiModelProperty(value = "规格基础id", dataType = "Long")
    private Long id;

    /**
     *数量
     */
    @JsonProperty("quantity")
    @ApiModelProperty(value = "数量", dataType = "Integer")
    private Integer quantity;

    /**
     *描述
     */
    @JsonProperty("description")
    @ApiModelProperty(value = "描述", dataType = "String")
    private String description;

    /**
     *定制说明配图
     */
    @JsonProperty("image")
    @ApiModelProperty(value = "定制说明配图", dataType = "String")
    private String image;


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