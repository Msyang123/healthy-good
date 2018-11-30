package com.lhiot.healthygood.domain.order;

import com.leon.microx.util.BeanUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class OrderProductParam {

    private Long standardId;
    private Integer price;
    @ApiModelProperty(notes = "购买份数",dataType = "Integer")
    private Integer productQty;

    public OrderProduct toProductObject(){
        OrderProduct orderProduct = new OrderProduct();
        BeanUtils.of(orderProduct).populate(BeanUtils.of(this).toMap());
        return orderProduct;
    }
}
