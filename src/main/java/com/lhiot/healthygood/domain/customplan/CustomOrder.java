package com.lhiot.healthygood.domain.customplan;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.lhiot.healthygood.type.CustomOrderBuyType;
import com.lhiot.healthygood.type.CustomOrderStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.List;

@Data
@ToString(callSuper = true)
@ApiModel(description = "创建定制计划订单对象")
public class CustomOrder {

    private Long id;

    private String customOrderCode;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    @ApiModelProperty(notes = "用户ID(多个以英文逗号分隔)", dataType = "String")
    private String userIds;

    @ApiModelProperty(notes = "WAIT_PAYMENT待付款、INVALID已失效、PAUSE_DELIVERY暂停配送、CUSTOMING定制中、FINISHED已结束", dataType = "CustomOrderStatus")
    private CustomOrderStatus status;

    @ApiModelProperty(notes = "WAIT_PAYMENT待付款、INVALID已失效、PAUSE_DELIVERY暂停配送、CUSTOMING定制中、FINISHED已结束", dataType = "String[]", readOnly = true)
    private String[] statusIn;

    @ApiModelProperty(value = "剩余配送次数",dataType = "Integer")
    private Integer remainingQty;

    @ApiModelProperty(value = "剩余配送次数退款时退回",dataType = "Integer")
    private Integer remainingQtyAdd;

    @ApiModelProperty(notes = "MANUAL-手动，AUTO-自动", dataType = "CustomOrderBuyType")
    private CustomOrderBuyType deliveryType;

    @ApiModelProperty(value = "配送总次数-周期数",dataType = "Integer")
    private Integer totalQty;

    @ApiModelProperty(value = "定制计划id",dataType = "Integer")
    @Min(value = 1L)
    private Long planId;

    @ApiModelProperty(value = "购买价格",dataType = "Integer")
    private Integer price;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间",dataType = "Date")
    private Date createAt;

    @ApiModelProperty(value = "x人套餐",dataType = "Integer")
    private Integer quantity;

    @ApiModelProperty(notes = "定制计划规格id", dataType = "Long")
    @Min(value = 1L)
    private Long specificationId;

    @ApiModelProperty(notes = "收货人", dataType = "String")
    @NotBlank(message = "收货人")
    private String receiveUser;

    @ApiModelProperty(notes = "联系电话", dataType = "String")
    @NotBlank(message = "联系电话")
    private String contactPhone;

    @ApiModelProperty(notes = "客户要求配送时间", dataType = "String")
    private String deliveryTime;

    @ApiModelProperty(notes = "送货地址", dataType = "String")
    @NotBlank(message = "送货地址不能为空")
    private String deliveryAddress;

    @ApiModelProperty(notes = "门店编码", dataType = "String")
    @NotBlank(message = "门店编码不能为空")
    private String storeCode;

    @ApiModelProperty(notes = "购买的定制计划", dataType = "CustomPlan")
    private CustomPlan customPlan;

    @ApiModelProperty(notes = "配送记录", dataType = "List")
    private List<CustomOrderDelivery> customOrderDeliveryList;

    @ApiModelProperty(notes = "下单用户昵称", dataType = "String")
    private String nickname;

    @ApiModelProperty(notes = "下单用户手机号码", dataType = "String")
    private String phone;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @ApiModelProperty(notes = "起始创建时间", dataType = "Date")
    private Date beginCreateAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @ApiModelProperty(notes = "截止创建时间", dataType = "Date")
    private Date endCreateAt;

    @ApiModelProperty(notes = "第三方支付产生的商户单号", dataType = "String")
    private String payId;

    @ApiModelProperty(notes = "定制计划规格描述", dataType = "String")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @ApiModelProperty(notes = "定制提取截止时间", dataType = "Date")
    private Date endExtractionAt;

    @ApiModelProperty(notes = "已暂停天数", dataType = "Long")
    private Long alreadyPauseDay;

    @ApiModelProperty(notes = "暂停配送设置对象", dataType = "CustomOrderPause")
    private CustomOrderPause customOrderPause;

    @ApiModelProperty(notes = "每页查询条数(为空或0不分页查所有)", dataType = "Integer")
    private Integer rows;
    @ApiModelProperty(notes = "当前页", dataType = "Integer")
    private Integer page;

    private Integer startRow;

    @JsonIgnore
    public Integer getStartRow() {
        if (this.rows != null && this.rows > 0) {
            return (this.page != null && this.page > 0 ? this.page - 1 : 0) * this.rows;
        }
        return null;
    }

}
