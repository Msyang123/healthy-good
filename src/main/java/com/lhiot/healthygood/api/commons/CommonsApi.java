package com.lhiot.healthygood.api.commons;

import com.alibaba.fastjson.JSONException;
import com.leon.microx.util.DateTime;
import com.leon.microx.util.Jackson;
import com.leon.microx.util.StringUtils;
import com.leon.microx.web.session.Sessions;
import com.lhiot.healthygood.domain.common.DeliverTime;
import com.lhiot.healthygood.domain.store.StoreResult;
import com.lhiot.healthygood.feign.DeliverServiceFeign;
import com.lhiot.healthygood.service.common.CommonService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 定制计划api /custom-plan-sections
 */
@Api(description = "定制计划接口")
@Slf4j
@RestController
public class CommonsApi {
    private final CommonService commonService;
    private final DeliverServiceFeign deliverServiceFeign;

    private static final LocalTime BEGIN_DELIVER_OF_DAY = LocalTime.parse("08:30:00");
    private static final LocalTime END_DELIVER_OF_DAY = LocalTime.parse("21:30:01");
    private static final DateTimeFormatter FULL = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public CommonsApi(CommonService commonService, DeliverServiceFeign deliverServiceFeign) {
        this.commonService = commonService;
        this.deliverServiceFeign = deliverServiceFeign;
    }

    //获取配送时间列表 定制订单使用
    @Sessions.Uncheck
    @GetMapping("/custom-plan-delivery/times")
    @ApiOperation(value = "获取订单配送时间 定制订单使用")
    public ResponseEntity<String> times() {
        List<DeliverTime> times = new ArrayList<>();

        LocalDateTime begin = LocalDate.now().atTime(BEGIN_DELIVER_OF_DAY);
        LocalDateTime latest = LocalDate.now().atTime(END_DELIVER_OF_DAY);
        LocalDateTime current = begin.withMinute(30);

        while (latest.compareTo(current) >= 0) {
            LocalDateTime next = current.plusHours(1);
            String display = StringUtils.format("{}-{}", current.format(FULL), next.format(FULL));
            times.add(DeliverTime.of(display, DateTime.convert(current), DateTime.convert(next)));
            current = next;
        }
        return ResponseEntity.ok(Jackson.json(times));
    }

    @Sessions.Uncheck
    @GetMapping("/delivery/times")
    @ApiOperation(value = "获取配送时间列表")
    public ResponseEntity<String> getDeliverTime() {
        //查询今天和明天的配送时间列表
        ResponseEntity<Map<String,Object>> deliverTimesEntity = deliverServiceFeign.deliverTimes(null);
        if(Objects.isNull(deliverTimesEntity)||deliverTimesEntity.getStatusCode().isError()){
            return ResponseEntity.badRequest().body("调用获取配送时间列表失败");
        }
        return ResponseEntity.ok(Jackson.json(deliverTimesEntity.getBody()));
    }

    @ApiOperation(value = "计算配送距离（收获地址与智能选择最近的门店）")
    @GetMapping("/delivery/distance")
    public ResponseEntity testDeliveryNote(@RequestParam("address") String address) throws JSONException {
        StoreResult storeResult = commonService.nearStore(address);
        return ResponseEntity.ok(storeResult);
    }
}
