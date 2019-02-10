package com.lhiot.healthygood.mq;

import com.leon.microx.amqp.RabbitInitializer;
import com.leon.microx.probe.collector.ProbeEventPublisher;
import com.leon.microx.util.Jackson;
import com.leon.microx.util.Maps;
import com.leon.microx.web.result.Tips;
import com.lhiot.healthygood.domain.customplan.CustomOrder;
import com.lhiot.healthygood.domain.customplan.CustomOrderPause;
import com.lhiot.healthygood.domain.customplan.CustomOrderTime;
import com.lhiot.healthygood.feign.model.DeliverTime;
import com.lhiot.healthygood.mapper.customplan.CustomOrderPauseMapper;
import com.lhiot.healthygood.service.customplan.CustomOrderService;
import com.lhiot.healthygood.type.CustomOrderBuyType;
import com.lhiot.healthygood.type.CustomOrderStatus;
import com.lhiot.healthygood.type.OperStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

/**
 * 自动提取定制订单消费类
 */
@Component
@Slf4j
public class AutoExtractionConsumer {

    private final CustomOrderService customOrderService;
    private final CustomOrderPauseMapper customOrderPauseMapper;
    private final ProbeEventPublisher publisher;
    private static final DateTimeFormatter MONTH_AND_DAY = DateTimeFormatter.ofPattern("MM-dd");

    public AutoExtractionConsumer(RabbitInitializer initializer, CustomOrderService customOrderService, CustomOrderPauseMapper customOrderPauseMapper, ProbeEventPublisher publisher) {
        this.customOrderService = customOrderService;
        this.customOrderPauseMapper = customOrderPauseMapper;
        this.publisher = publisher;
        //HealthyGoodQueue.DelayQueue.AUTO_EXTRACTION.init(initializer);
    }

    /**
     * 每天自动提取定制订单(消费端)
     * 注：自动配送还需要自动创建提取订单
     *
     * @param customOrderCode
     */
    @RabbitHandler
    @RabbitListener(queues = HealthyGoodQueue.DelayQueue.AUTO_EXTRACTION_CONSUMER)
    public void autoExtractionConsumer(String customOrderCode) {
        CustomOrder customOrder = customOrderService.selectByCode(customOrderCode);
        if (Objects.isNull(customOrder) || customOrder.getRemainingQty() <= 0
                || Objects.equals(customOrder.getStatus(), CustomOrderStatus.WAIT_PAYMENT)
                || Objects.equals(customOrder.getStatus(), CustomOrderStatus.INVALID)
                || Objects.equals(customOrder.getStatus(), CustomOrderStatus.FINISHED)) {
            log.info("每天发订单配送,定制订单不存在或者定制订单剩余次数为0或者定制订单非暂停和恢复状态{},{}", customOrderCode, customOrder);
            return;
        }
        Date current = Date.from(Instant.now());
        LocalDate now = LocalDate.now();
        //获取设置的配送时间段 customOrder.getDeliveryTime() eg:{"display":"08:30-09:30","startTime":"08:30:00","endTime":"09:30:00"}
        CustomOrderTime customOrderTime = Jackson.object(customOrder.getDeliveryTime(), CustomOrderTime.class);
        //设置具体配送时间
        LocalDateTime nextDeliverTime = now.plusDays(1).atTime(customOrderTime.getStartTime());
        try {
            //检查是否用户设置了暂停配送并且当前时间在暂停配送时间内
            CustomOrderPause customOrderPause = new CustomOrderPause();
            customOrderPause.setOperStatus(OperStatus.PAUSE);
            customOrderPause.setCustomOrderCode(customOrderCode);
            customOrderPause.setPauseBeginAt(current);
            customOrderPause.setPauseEndAt(current);
            CustomOrderPause searchCustomOrderPause = customOrderPauseMapper.selectCustomOrderPause(customOrderPause);
            //检测到在暂停时间内或者定制在暂停中
            if (Objects.nonNull(searchCustomOrderPause) || Objects.equals(customOrder.getStatus(), CustomOrderStatus.PAUSE_DELIVERY)) {
                log.warn("每天发订单配送,当前时间有暂停定制订单设置:{}", searchCustomOrderPause);
                // 下一次配送【明天配送】
                //继续下一次配送
                customOrderService.scheduleDeliveryCustomOrder(nextDeliverTime, customOrderCode);
                return;
            }
            //自动配送需要提取定制订单
            if (Objects.equals(customOrder.getDeliveryType(), CustomOrderBuyType.AUTO)) {
                //将定制设定时间转换成今天的配送时间
                DeliverTime deliverTime = new DeliverTime();
                String timeDisplay = customOrderTime.getDisplay();
                String[] timeParts = timeDisplay.split("-");
                timeDisplay = now.format(MONTH_AND_DAY) + " " + timeParts[0] + ":00-" + now.format(MONTH_AND_DAY) + " " + timeParts[1] + ":00";
                deliverTime.setDisplay(timeDisplay);//定制订单显示格式 eg:08:30-09:30 应该显示为eg 12-27 20:30:11-12-27 21:30:11
                deliverTime.setStartTime(Date.from(now.atTime(customOrderTime.getStartTime()).atZone(ZoneId.systemDefault()).toInstant()));
                deliverTime.setEndTime(Date.from(now.atTime(customOrderTime.getEndTime()).atZone(ZoneId.systemDefault()).toInstant()));

                Tips result = customOrderService.extraction(customOrder, Jackson.json(deliverTime), "自动提取");//会提取并且发送海鼎与配送
                if (result.err()) {
                    log.error("每天发订单配送,提取定制订单失败:{}", result);
                    return;
                } else {
                    // 下一次配送
                    customOrderService.scheduleDeliveryCustomOrder(nextDeliverTime, customOrderCode);
                    log.error("每天发订单配送,提取定制订单成功,并且已发送明天配送队列信息:{},{},{}", result, nextDeliverTime, customOrderCode);
                }
            }
        } catch (Exception e) {
            // 异常、队列消息
            publisher.mqConsumerException(e, Maps.of("message", "自动提取失败"));
        }
    }
}
