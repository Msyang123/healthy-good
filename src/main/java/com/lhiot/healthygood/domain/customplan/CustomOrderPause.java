package com.lhiot.healthygood.domain.customplan;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.Date;

@Data
@ToString(callSuper = true)
@ApiModel(description = "创建定制计划订单暂停对象")
public class CustomOrderPause {

    @ApiModelProperty(hidden = true)
    private Long id;

    @ApiModelProperty(hidden = true, notes = "定制计划订单id")
    private Long customOrderId;

    @ApiModelProperty(value = "创建时间", dataType = "Date", hidden = true)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createAt;

    @ApiModelProperty(value = "暂停开始时间", dataType = "Date", hidden = true)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date pauseBeginAt;

    @ApiModelProperty(notes = "暂停结束时间", dataType = "Long", hidden = true)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date pauseEndAt;

    @ApiModelProperty(value = "前端传递暂停开始时间格式yyyy-MM-dd", dataType = "String")
    @NotBlank(message ="不能为空")
    private String pauseBegin;

    @ApiModelProperty(notes = "暂停天数", dataType = "Long")
    @Min(value = 1)
    private Long pauseDay;
}
