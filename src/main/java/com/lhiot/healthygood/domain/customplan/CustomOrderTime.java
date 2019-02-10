package com.lhiot.healthygood.domain.customplan;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;

/**
 * 定制订单创建时间对象 无日期信息
 */
@Data
@ApiModel
@ToString
public class CustomOrderTime {

    @ApiModelProperty(notes = "配送动作", dataType = "String", required = true, example = "立即配送")
    private String display;

    @ApiModelProperty(notes = "开始时间", dataType = "Date", required = true, example = "HH:mm:ss")
    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;

    @ApiModelProperty(notes = "结束时间", dataType = "Date", required = true, example = "HH:mm:ss")
    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;

    public static CustomOrderTime of(String display, LocalTime start, LocalTime end){
        CustomOrderTime deliverTime = new CustomOrderTime();
        deliverTime.display = display;
        deliverTime.startTime = start;
        deliverTime.endTime = end;
        return deliverTime;
    }
}
