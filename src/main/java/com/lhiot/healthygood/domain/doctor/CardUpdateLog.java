package com.lhiot.healthygood.domain.doctor;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lhiot.healthygood.common.PagerRequestObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
* Description:
* @author yijun
* @date 2018/07/26
*/
@Data
@ToString(callSuper = true)
@ApiModel
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CardUpdateLog extends PagerRequestObject {

    /**
    *id
    */
    @JsonProperty("id")
    @ApiModelProperty(value = "id", dataType = "Long")
    private Long id;

    /**
    *银行卡卡号
    */
    @JsonProperty("cardNo")
    @ApiModelProperty(value = "银行卡卡号", dataType = "String")
    private String cardNo;

    /**
    *开户行
    */
    @JsonProperty("bankDeposit")
    @ApiModelProperty(value = "开户行", dataType = "String")
    private String bankDeposit;

    /**
    *持卡人姓名
    */
    @JsonProperty("cardUsername")
    @ApiModelProperty(value = "持卡人姓名", dataType = "String")
    private String cardUsername;

    /**
    *修改时间
    */
    @JsonProperty("updateTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @ApiModelProperty(value = "修改时间", dataType = "Date")
    private java.util.Date updateAt;
    

    /**
    *鲜果师id
    */
    @JsonProperty("doctorId")
    @ApiModelProperty(value = "鲜果师id", dataType = "Long")
    private Long doctorId;

}
