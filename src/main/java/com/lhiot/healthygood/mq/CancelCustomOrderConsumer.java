package com.lhiot.healthygood.mq;

import com.leon.microx.amqp.RabbitInitializer;
import com.leon.microx.probe.collector.ProbeEventPublisher;
import com.leon.microx.util.Maps;
import com.lhiot.healthygood.domain.customplan.CustomOrder;
import com.lhiot.healthygood.mapper.customplan.CustomOrderMapper;
import com.lhiot.healthygood.type.CustomOrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 取消未支付定制订单
 */
@Component
@Slf4j
public class CancelCustomOrderConsumer {

    private final CustomOrderMapper customOrderMapper;
    private final ProbeEventPublisher publisher;
    public CancelCustomOrderConsumer(RabbitInitializer initializer, CustomOrderMapper customOrderMapper, ProbeEventPublisher publisher) {
        this.customOrderMapper = customOrderMapper;
        this.publisher = publisher;
        HealthyGoodQueue.DelayQueue.CANCEL_CUSTOM_ORDER.init(initializer);
    }

    /**
     * 取消未支付定制订单 订单状态修改为已失效
     *
     * @param customOrderCode
     */
    @RabbitHandler
    @RabbitListener(queues = HealthyGoodQueue.DelayQueue.CANCEL_CUSTOM_ORDER_CONSUMER)
    public void cancelUnPayOrder(String customOrderCode) {
        try {
            CustomOrder searchCustomOrder = customOrderMapper.selectByCode(customOrderCode);
            if(Objects.isNull(searchCustomOrder)){
                log.warn("定制订单超过30分钟未支付，未找到定制订单{}",customOrderCode);
            }else if(Objects.equals(CustomOrderStatus.WAIT_PAYMENT,searchCustomOrder.getStatus())){
                CustomOrder customOrder=new CustomOrder();
                customOrder.setStatus(CustomOrderStatus.INVALID);
                customOrder.setCustomOrderCode(customOrderCode);
                customOrderMapper.updateByCode(customOrder);
                log.info("定制订单超过30分钟未支付，已取消{}",customOrderCode);
            }else{
                log.info("定制订单超过30分钟未支付检查，已支付{}",customOrderCode);
            }
        } catch (Exception e) {
            // 异常、队列消息
            publisher.mqConsumerException(e, Maps.of("message", "定制订单状态修改为已失效失败"));
        }
    }
}
