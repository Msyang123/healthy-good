package com.lhiot.healthygood.domain.doctor;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Description:收入统计实体
 *
 * @author yijun
 * @date 2018/07/26
 */
@Data
@ToString(callSuper = true)
@ApiModel
@NoArgsConstructor
public class IncomeStat {

    /**
     * 已获得红利
     */
    @JsonProperty("bonus")
    @ApiModelProperty(value = "已获得红利", dataType = "Long")
    private Long bonus;


    /**
     * 可结算红利
     */
    @JsonProperty("bonusCanBeSettled")
    @ApiModelProperty(value = "可结算红利", dataType = "Long")
    private Long bonusCanBeSettled;

    /**
     * 历史总收入
     */
    @JsonProperty("bonusOfHistory")
    @ApiModelProperty(value = "历史总收入", dataType = "Long")
    private Long bonusOfHistory;


    /**
     * 已结算总额
     */
    @JsonProperty("bonusSettled")
    @ApiModelProperty(value = "已结算总额", dataType = "Long")
    private Long bonusSettled;

    /**
     * 正在结算
     */
    @JsonProperty("bonusSettling")
    @ApiModelProperty(value = "正在结算", dataType = "Long")
    private Long bonusSettling;

}
