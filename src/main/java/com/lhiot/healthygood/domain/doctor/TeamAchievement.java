package com.lhiot.healthygood.domain.doctor;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@ApiModel(description = "鲜果师团队中个人业绩统计")
@NoArgsConstructor
public class TeamAchievement {
    @ApiModelProperty(notes = "鲜果师名称", dataType = "String")
    private String realName;

    @ApiModelProperty(notes = "手机号", dataType = "String")
    private String phone;

    @ApiModelProperty(notes = "头像", dataType = "String")
    private String avatar;

    @ApiModelProperty(notes = "加盟时间", dataType = "Date")
    @JsonProperty("createAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createAt;

    @ApiModelProperty(notes = "总销售额", dataType = "Long")
    private Long salesAmount;

    @ApiModelProperty(notes = " 当月销售额", dataType = "Long")
    private Long amountOfMonth;

    @ApiModelProperty(notes = "本月产生红利", dataType = "Long")
    private Long incomeAmountOfMonth;

}
