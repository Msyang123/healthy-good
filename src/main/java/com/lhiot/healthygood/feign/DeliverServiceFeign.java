package com.lhiot.healthygood.feign;

import com.leon.microx.predefine.Day;
import com.leon.microx.web.result.Tips;
import com.lhiot.healthygood.feign.model.DeliverFeeQuery;
import com.lhiot.healthygood.feign.model.DeliverOrder;
import com.lhiot.healthygood.feign.model.DeliverUpdate;
import com.lhiot.healthygood.feign.type.CoordinateSystem;
import com.lhiot.healthygood.feign.type.DeliverType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Component
@FeignClient("delivery-service-v1-0")
public interface DeliverServiceFeign {

    /**
     * 发送配送单 已废弃
     *
     * @see OrderServiceFeign updateOrderToDelivery()
     */
    @Deprecated
    @RequestMapping(value = "/{deliverType}/delivery-notes", method = RequestMethod.POST)
    ResponseEntity create(@PathVariable("deliverType") DeliverType type, @RequestParam("coordinate") CoordinateSystem coordinate, @RequestBody DeliverOrder deliverOrder);

    //更新配送单
    @RequestMapping(value = "/delivery-notes/{code}", method = RequestMethod.PUT)
    ResponseEntity<String> update(@PathVariable("code") String code, @RequestBody DeliverUpdate deliverUpdate);

    //配送单详细信息
    @RequestMapping(value = "/{deliverType}/delivery-notes/{code}", method = RequestMethod.GET)
    ResponseEntity<String> detail(@PathVariable("deliverType") DeliverType type, @PathVariable("code") String code);

    //配送单回调签名验证
    @RequestMapping(value = "/{deliverType}/back-signature", method = RequestMethod.POST)
    ResponseEntity<Tips> backSignature(@PathVariable("deliverType") DeliverType type, @RequestBody Map<String, String> params);

    //获取订单配送时间列表
    @RequestMapping(value = "/delivery/times", method = RequestMethod.GET)
    ResponseEntity<Map<String, Object>> deliverTimes(@RequestParam(value = "date", required = false) Day day);

    //计算配送费
    @RequestMapping(value = "/delivery/fee/search", method = RequestMethod.POST)
    ResponseEntity search(@RequestBody DeliverFeeQuery feeQuery);
}
