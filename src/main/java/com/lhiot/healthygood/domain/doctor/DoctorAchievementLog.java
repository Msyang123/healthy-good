package com.lhiot.healthygood.domain.doctor;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.leon.microx.util.BeanUtils;
import com.lhiot.healthygood.type.IncomeType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

/**
* Description:鲜果师业绩记录实体类
* @author yijun
* @date 2018/07/26
*/
@Data
@ToString(callSuper = true)
@ApiModel
@NoArgsConstructor
public class DoctorAchievementLog{

    /**
    *
    */
    @JsonProperty("id")
    @ApiModelProperty(value = "", dataType = "Long")
    private Long id;

    /**
    *鲜果师编号
    */
    @JsonProperty("doctorId")
    @ApiModelProperty(value = "鲜果师编号", dataType = "Long")
    private Long doctorId;

    /**
    *消费用户编号
    */
    @JsonProperty("userId")
    @ApiModelProperty(value = "消费用户编号", dataType = "Long")
    private Long userId;

    /**
    *订单编号
    */
    @JsonProperty("orderId")
    @ApiModelProperty(value = "订单编号", dataType = "Long")
    private Long orderId;

    /**
    *业绩金额
    */
    @JsonProperty("amount")
    @ApiModelProperty(value = "业绩金额", dataType = "Integer")
    private Integer amount;

    /**
    *红利收支类型:订单分成(收入)- ORDER,分销商分成(收入)-SUB_DISTRIBUTOR 红利结算(支出)-SETTLEMENT 客户退款(支出)-REFUND
    */
    @JsonProperty("sourceType")
    @ApiModelProperty(value = "红利收支类型:订单分成(收入)- ORDER,分销商分成(收入)-SUB_DISTRIBUTOR 红利结算(支出)-SETTLEMENT 客户退款(支出)-REFUND", dataType = "String")
    private String sourceType;

    /**
    *创建时间
    */
    @JsonProperty("createAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间", dataType = "Date")
    private java.util.Date createAt;


    /**
     *销售提成
     */
    @JsonProperty("commission")
    @ApiModelProperty(value = "销售提成", dataType = "Double")
    private Double commission;


    /**
     *鲜果师提成
     */
    @JsonProperty("fruitDoctorCommission")
    @ApiModelProperty(value = "鲜果师提成", dataType = "Double")
    private Double fruitDoctorCommission;

    /**
     *收入支出类型(INCOME -- 收入，EXPENDITURE – 支出，默认为全部)
     */
    @JsonProperty("incomeType")
    @ApiModelProperty(value = "收入支出类型(INCOME -- 收入，EXPENDITURE – 支出，默认为全部)", dataType = "IncomeType")
    private IncomeType incomeType;
    
    @ApiModelProperty(value = "提成金额，包括鲜果师红利及提成", dataType = "map")
    private Map<String,Object> commissionMap;

    public DoctorAchievementLog toDoctorBonusLog(){
    	DoctorAchievementLog doctorAchievementLog = new DoctorAchievementLog();
        BeanUtils.of(doctorAchievementLog).populate(BeanUtils.of(this).toMap());
        return doctorAchievementLog;
    }


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
