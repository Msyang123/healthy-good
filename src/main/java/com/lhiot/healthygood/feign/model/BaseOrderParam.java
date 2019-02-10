package com.lhiot.healthygood.feign.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lhiot.healthygood.feign.type.ApplicationType;
import com.lhiot.healthygood.feign.type.OrderStatus;
import com.lhiot.healthygood.feign.type.OrderType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author yj  created in  2018/12/8 11:28
 */
@ApiModel
@Data
public class BaseOrderParam {
    @ApiModelProperty(notes = "用户ID(多个以英文逗号分隔)", dataType = "String")
    private String userIds;
    @ApiModelProperty(notes = "订单类型", dataType = "OrderType")
    private OrderType orderType;
    @ApiModelProperty(notes = "订单状态", dataType = "OrderStatus[]")
    private OrderStatus[] orderStatuses;
    @ApiModelProperty(notes = "每页查询条数(为空或0不分页查所有)", dataType = "Integer")
    private Integer rows;
    @ApiModelProperty(notes = "当前页", dataType = "Integer")
    private Integer page;
    @ApiModelProperty(notes = "订单编号",dataType = "String")
    private String orderCode;
    @ApiModelProperty(notes = "phone",dataType = "String")
    private String phone;
    @ApiModelProperty(notes = "订单编号集合",dataType = "List")
    private List<String> orderCodeList;

    private ApplicationType applicationType;

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
