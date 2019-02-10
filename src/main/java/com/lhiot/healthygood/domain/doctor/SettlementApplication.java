package com.lhiot.healthygood.domain.doctor;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lhiot.healthygood.type.SettlementStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

/**
 * Description:结算申请实体类
 *
 * @author yijun
 * @date 2018/07/26
 */
@Data
@ToString(callSuper = true)
@ApiModel
@NoArgsConstructor
public class SettlementApplication {

    /**
     * id
     */
    @JsonProperty("id")
    @ApiModelProperty(value = "id", dataType = "Long")
    private Long id;

    @JsonProperty("openId")
    @ApiModelProperty(value = "openId", dataType = "String")
    private String openId;

    /**
     * 申请人（鲜果师编号）
     */
    @JsonProperty("doctorId")
    @ApiModelProperty(value = "申请人（鲜果师编号）", dataType = "Long")
    private Long doctorId;

    /**
     * 申请时间
     */
    @JsonProperty("createAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @ApiModelProperty(value = "申请时间", dataType = "Date")
    private java.util.Date createAt;

    /**
     * 申请提取金额
     */
    @JsonProperty("amount")
    @ApiModelProperty(value = "申请提取金额", dataType = "Integer")
    private Integer amount;

    /**
     * 结算状态(UNSETTLED-未处理  SUCCESS-已成功  EXPIRED-已过期)
     */
    @JsonProperty("settlementStatus")
    @ApiModelProperty(value = "结算状态(UNSETTLED-未处理  SUCCESS-已成功  EXPIRED-已过期)", dataType = "SettlementStatus")
    private SettlementStatus settlementStatus;

    /**
     * 处理时间
     */
    @JsonProperty("dealAt")
    @ApiModelProperty(value = "处理时间", dataType = "Date")
    private java.util.Date dealAt;

    /**
     * 起始创建时间
     */
    @JsonProperty("beginCreateAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @ApiModelProperty(notes = "起始创建时间(用于搜索)", dataType = "Date")
    private Date beginCreateAt;

    /**
     * 截止创建时间
     */
    @JsonProperty("endCreateAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @ApiModelProperty(notes = "截止创建时间(用于搜索)", dataType = "Date")
    private Date endCreateAt;

    /**
     * 申请人（鲜果师名称）
     */
    @JsonProperty("realName")
    @ApiModelProperty(value = "申请人（鲜果师名称）", dataType = "String")
    private String realName;

    /**
     * 申请人（鲜果师手机号）
     */
    @JsonProperty("phone")
    @ApiModelProperty(value = "申请人（鲜果师手机号）", dataType = "String")
    private String phone;

    /**
     * 银行卡卡号
     */
    @JsonProperty("cardNo")
    @ApiModelProperty(value = "银行卡卡号", dataType = "String")
    private String cardNo;

    /**
     * 开户行
     */
    @JsonProperty("bankDeposit")
    @ApiModelProperty(value = "开户行", dataType = "String")
    private String bankDeposit;

    /**
     * 持卡人姓名
     */
    @JsonProperty("cardUsername")
    @ApiModelProperty(value = "持卡人姓名", dataType = "String")
    private String cardUsername;


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
