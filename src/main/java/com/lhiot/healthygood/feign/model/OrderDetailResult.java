package com.lhiot.healthygood.feign.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lhiot.healthygood.feign.type.*;
import com.lhiot.healthygood.type.ReceivingWay;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author zhangfeng created in 2018/9/19 15:39
 **/
@Data
@ApiModel
public class OrderDetailResult {
    @ApiModelProperty(notes = "订单Id",dataType = "Long")
    private Long id;
    @ApiModelProperty(notes = "订单编码", dataType = "String")
    private String code;
    @ApiModelProperty(notes = "用户Id", dataType = "Long")
    private Long userId;
    @ApiModelProperty(notes = "应用类型", dataType = "ApplicationTypeEnum")
    private ApplicationType applicationType;
    @ApiModelProperty(notes = "订单类型", dataType = "OrderType")
    private OrderType orderType;
    @ApiModelProperty(notes = "提货方式", dataType = "String")
    private ReceivingWay receivingWay;
    @ApiModelProperty(notes = "订单总金额", dataType = "Integer")
    private Integer totalAmount;
    @ApiModelProperty(notes = "订单应付金额", dataType = "Integer")
    private Integer amountPayable;
    @ApiModelProperty(notes = "配送费", dataType = "Integer")
    private Integer deliveryAmount;
    @ApiModelProperty(notes = "优惠金额", dataType = "Integer")
    private Integer couponAmount;
    @ApiModelProperty(notes = "海鼎状态", dataType = "HdStatus")
    private HdStatus hdStatus = HdStatus.NOT_SEND;
    @ApiModelProperty(notes = "海鼎备货时间",dataType = "Date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date hdStockAt;
    @ApiModelProperty(notes = "订单状态", dataType = "CustomOrderStatus")
    private OrderStatus status = OrderStatus.WAIT_PAYMENT;
    @ApiModelProperty(notes = "收货人", dataType = "String")
    private String receiveUser;
    @ApiModelProperty(notes = "收货人联系方式", dataType = "String")
    private String contactPhone;
    @ApiModelProperty(notes = "收货地址：门店自提订单填写门店地址", dataType = "String")
    private String address;
    @ApiModelProperty(notes = "备注", dataType = "String")
    private String remark;
    @ApiModelProperty(notes = "提货截止时间", dataType = "String")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date deliveryEndTime;
    @ApiModelProperty(notes = "海鼎的订单编码", dataType = "String")
    private String hdOrderCode;
    @ApiModelProperty(notes = "用户昵称", dataType = "String")
    private String nickname;
    @ApiModelProperty(notes = "配送时间段  eg {display:'立即配送',startTime:'2018-12-22 00:00:00',endTime:'2018-12-22 01:00:00'}", dataType = "String")
    private String deliverAt;
    @ApiModelProperty(notes = "是否允许退款YES是NO否", dataType = "AllowRefund")
    private AllowRefund allowRefund = AllowRefund.YES;
    @ApiModelProperty(notes = "订单创建时间",dataType = "Date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createAt;
    @ApiModelProperty(notes = "支付Id",dataType = "String")
    private String payId;
    @ApiModelProperty(notes = "支付类型", dataType = "TradeType")
    private TradeType tradeType;
    @ApiModelProperty(notes = "订单商品", dataType = "List")
    private List<OrderProduct> orderProductList;
    @ApiModelProperty(notes = "订单门店信息", dataType = "OrderStore")
    private OrderStore orderStore;
    @ApiModelProperty(notes = "订单状态流水列表", dataType = "List")
    private List<OrderFlow> orderFlowList;
}
