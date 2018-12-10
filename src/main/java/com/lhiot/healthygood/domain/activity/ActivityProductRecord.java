package com.lhiot.healthygood.domain.activity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@ApiModel(description = "新品尝鲜活动")
@NoArgsConstructor
public class ActivityProductRecord{
    @ApiModelProperty(notes="ID",dataType="Long")
    private Long id;

    @ApiModelProperty(notes="userId",dataType="Long")
    private Long userId;

    @JsonProperty("shelfId")
    @ApiModelProperty(notes="商品上架ID",dataType="Long")
    private Long productShelfId;

    @ApiModelProperty(notes="订单编号",dataType="Long")
    private String orderCode;

    @ApiModelProperty(notes="活动ID",dataType="Long")
    private Long activityId;

    @ApiModelProperty(notes="活动类型",dataType="Long")
    private String activityType;

    @ApiModelProperty(notes = "每页查询条数(为空或0不分页查所有)", dataType = "Integer")
    private Integer rows;
    @ApiModelProperty(notes = "当前页", dataType = "Integer")
    private Integer page;

    @ApiModelProperty(hidden = true)
    private Integer startRow;

    /**
     *创建时间
     */
    @JsonProperty("createAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间", dataType = "Date", readOnly = true)
    private java.util.Date createAt;

    @JsonIgnore
    public Integer getStartRow() {
        if (this.rows != null && this.rows > 0) {
            return (this.page != null && this.page > 0 ? this.page - 1 : 0) * this.rows;
        }
        return null;
    }
}
