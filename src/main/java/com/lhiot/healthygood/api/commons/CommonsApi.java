package com.lhiot.healthygood.api.commons;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.leon.microx.util.Converter;
import com.leon.microx.util.ImmutableMap;
import com.leon.microx.util.Jackson;
import com.leon.microx.web.session.Sessions;
import com.lhiot.healthygood.domain.common.DeliverTimeItem;
import com.lhiot.healthygood.domain.common.StoreResult;
import com.lhiot.healthygood.service.common.CommonService;
import com.lhiot.healthygood.service.customplan.CustomPlanService;
import com.lhiot.healthygood.util.BaiduMapUtil;
import com.lhiot.healthygood.util.DateFormatUtil;
import com.lhiot.healthygood.util.MapUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * 定制计划api /custom-plan-sections
 */
@Api(description = "定制计划接口")
@Slf4j
@RestController
public class CommonsApi {
    private final CommonService commonService;
    @Autowired
    public CommonsApi(CommonService commonService){
        this.commonService = commonService;
    }
    //获取配送时间列表 定制订单使用
    @Sessions.Uncheck
    @GetMapping("/custom-plan-delivery/times")
    @ApiOperation(value = "获取订单配送时间 定制订单使用")
    public ResponseEntity<String> times() throws JsonProcessingException {
        Map<String,Map> timeResult=new HashMap<>();
        Date current=new Date();
        String today = DateFormatUtil.dateToString(current,"yyyy-MM-dd");
        Calendar calendarOfTomorrow = Calendar.getInstance();
        calendarOfTomorrow.add(Calendar.DATE,1);
        String tomorrow =DateFormatUtil.dateToString(calendarOfTomorrow.getTime(),"yyyy-MM-dd");
        Date tonightBegin = DateFormatUtil.stringToDate(today+" 21:31:00","yyyy-MM-dd HH:mm:ss");
        Date tonightEnd = DateFormatUtil.stringToDate(tomorrow+" 08:29:59","yyyy-MM-dd HH:mm:ss");
        Date end = DateFormatUtil.stringToDate(tomorrow+" 21:30:01","yyyy-MM-dd HH:mm:ss");
        Date start = DateFormatUtil.stringToDate(today+" 07:30:00","yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        String nextStartTime=null;
        String startTime=DateFormatUtil.dateToString(calendar.getTime(),"yyyy-MM-dd ")+"07:30:00";;
        List<DeliverTimeItem> todayTimeList=new ArrayList<>();
        List<DeliverTimeItem> tomorrowTimeList=new ArrayList<>();
        boolean firstLable=false;
        while (true){
            //每次加一个小时
            calendar.add(Calendar.HOUR_OF_DAY,1);
            //今天晚上9:30-明天早上8:30 不配送
            if(calendar.getTime().after(tonightBegin)&&calendar.getTime().before(tonightEnd)){
                continue;
            }
            System.out.print("");
            //结束时间
            if(calendar.getTime().after(end)){
                break;
            }
            nextStartTime=DateFormatUtil.dateToString(calendar.getTime(),"yyyy-MM-dd HH")+":30:00";
            DeliverTimeItem deliverTimeItem = new DeliverTimeItem(
                    (DateFormatUtil.dateToString(DateFormatUtil.stringToDate(startTime,"yyyy-MM-dd HH:mm:ss"),"HH:mm")+
                            "-"+
                            DateFormatUtil.dateToString(DateFormatUtil.stringToDate(nextStartTime,"yyyy-MM-dd HH:mm:ss"),"HH:mm")),
                    DateFormatUtil.dateToString(DateFormatUtil.stringToDate(startTime,"yyyy-MM-dd HH:mm:ss"),"HH:mm"),
                    DateFormatUtil.dateToString(DateFormatUtil.stringToDate(nextStartTime,"yyyy-MM-dd HH:mm:ss"),"HH:mm"));
            firstLable = false;
            if(calendar.getTime().before(tonightBegin)){
                todayTimeList.add(deliverTimeItem);
            }else{
                tomorrowTimeList.add(deliverTimeItem);
            }
            startTime= nextStartTime;
        }
        return ResponseEntity.ok(Jackson.json(todayTimeList));
    }

    //获取配送时间列表
    @Sessions.Uncheck
    @GetMapping("/delivery/times")
    @ApiOperation(value = "获取订单配送时间")
    public ResponseEntity<String> getDeliverTime() throws JsonProcessingException {
        Map<String,Map> timeResult=new HashMap<>();
        Date current=new Date();
        String today = DateFormatUtil.dateToString(current,"yyyy-MM-dd");
        Calendar calendarOfTomorrow = Calendar.getInstance();
        calendarOfTomorrow.add(Calendar.DATE,1);
        String tomorrow =DateFormatUtil.dateToString(calendarOfTomorrow.getTime(),"yyyy-MM-dd");
        Date tonightBegin = DateFormatUtil.stringToDate(today+" 21:31:00","yyyy-MM-dd HH:mm:ss");
        Date tonightEnd = DateFormatUtil.stringToDate(tomorrow+" 08:29:59","yyyy-MM-dd HH:mm:ss");
        Date end = DateFormatUtil.stringToDate(tomorrow+" 21:30:01","yyyy-MM-dd HH:mm:ss");

        Calendar calendar = Calendar.getInstance();
        String nextStartTime=null;
        String startTime=null;
        List<DeliverTimeItem> todayTimeList=new ArrayList<>();
        List<DeliverTimeItem> tomorrowTimeList=new ArrayList<>();
        boolean firstLable=true;
        while (true){
            startTime=DateFormatUtil.dateToString(calendar.getTime(),"yyyy-MM-dd HH")+":30:00";
            //每次加一个小时
            calendar.add(Calendar.HOUR_OF_DAY,1);
            //今天晚上9:30-明天早上8:30 不配送
            if(calendar.getTime().after(tonightBegin)&&calendar.getTime().before(tonightEnd)){
                continue;
            }
            //结束时间
            if(calendar.getTime().after(end)){
                break;
            }
            nextStartTime=DateFormatUtil.dateToString(calendar.getTime(),"yyyy-MM-dd HH")+":30:00";
            DeliverTimeItem deliverTimeItem = new DeliverTimeItem(
                    firstLable?"立即配送":(DateFormatUtil.dateToString(DateFormatUtil.stringToDate(startTime,"yyyy-MM-dd HH:mm:ss"),"HH:mm")+
                            "-"+
                            DateFormatUtil.dateToString(DateFormatUtil.stringToDate(nextStartTime,"yyyy-MM-dd HH:mm:ss"),"HH:mm")), startTime, nextStartTime);
            firstLable = false;
            if(calendar.getTime().before(tonightBegin)){
                todayTimeList.add(deliverTimeItem);
            }else{
                tomorrowTimeList.add(deliverTimeItem);
            }
        }
        timeResult.put("today", ImmutableMap.of("value",todayTimeList,"date",DateFormatUtil.dateToString(current,"MM-dd")));
        timeResult.put("tomorrow",ImmutableMap.of("value",tomorrowTimeList,"date",DateFormatUtil.dateToString(calendarOfTomorrow.getTime(),"MM-dd")));
/*        {
            "today":
            {"value":[{"display":"立即配送","startTime":"2017-01-02 12:33:34","endTime":"2017-01-02 12:33:34"}],"date":"08-15"}
            "tomorrow":
            {"value":[{"display":"121","startTime":"2017-01-02 12:33:34","endTime":"2017-01-02 12:33:34"}],"date":"08-16"}
        }*/
        return ResponseEntity.ok(Jackson.json(timeResult));
    }
    //计算配送距离（收获地址与智能选择最近的门店）
//    /delivery/distance
//    计算配送距离（收获地址与智能选择最近的门店）
    @ApiOperation(value="")
    @GetMapping("/delivery/distance")
    public ResponseEntity testDeliveryNote(@RequestParam("address")String address) throws JSONException {
        StoreResult storeResult = commonService.nearStore(address);
        return ResponseEntity.ok(storeResult);
    }
}
