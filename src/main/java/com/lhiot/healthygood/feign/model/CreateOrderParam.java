package com.lhiot.healthygood.feign.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lhiot.healthygood.feign.type.AllowRefund;
import com.lhiot.healthygood.feign.type.ApplicationType;
import com.lhiot.healthygood.feign.type.OrderType;
import com.lhiot.healthygood.type.ReceivingWay;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@Data
@ApiModel
@NotNull
public class CreateOrderParam {

    private Long userId;
    private ApplicationType applicationType;
    private OrderType orderType;
    private ReceivingWay receivingWay;
    private Integer couponAmount = 0;
    private Integer totalAmount;
    @ApiModelProperty(notes = "配送费", dataType = "Integer")
    private Integer deliveryAmount = 0;
    private Integer amountPayable = 0;
    @ApiModelProperty(notes = "收货地址：门店自提订单填写收货地址", dataType = "String")
    private String address;
    @ApiModelProperty(notes = "收货人", dataType = "String")
    private String receiveUser;
    @ApiModelProperty(notes = "收货人昵称", dataType = "String")
    private String nickname;
    @ApiModelProperty(notes = "收货人联系方式", dataType = "String")
    private String contactPhone;
    private String remark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @ApiModelProperty(notes = "提货截止时间", dataType = "Date")
    private Date deliveryEndAt;
    @ApiModelProperty(notes = "配送时间 json格式如 {\"display\":\"立即配送\",\"startTime\":\"2018-08-15 11:30:00\",\"endTime\":\"2018-08-15 12:30:00\"}", dataType = "String")
    private String deliverAt;
    @ApiModelProperty(notes = "是否允许退款YES是NO否", dataType = "AllowRefund")
    private AllowRefund allowRefund;
    @ApiModelProperty(notes = "第三方支付产生的商户单号", dataType = "String")
    private String payId;

    @ApiModelProperty(notes = "商品列表", dataType = "OrderProduct")
    @NotNull
    @Size(min=1,max = 1000)
    private List<OrderProduct> orderProducts;
    @ApiModelProperty(notes = "门店信息", dataType = "OrderStoreParam")
    private OrderStore orderStore;
}
