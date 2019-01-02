package com.lhiot.healthygood.mq;

import com.leon.microx.amqp.RabbitInitializer;
import com.leon.microx.probe.collector.ProbeEventPublisher;
import com.leon.microx.util.Maps;
import com.lhiot.healthygood.feign.OrderServiceFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.rmi.ServerException;
import java.util.Objects;

/**
 * 自动发货消费类
 */
@Component
@Slf4j
public class SendToHdConsumer {

    private final OrderServiceFeign orderServiceFeign;
    private final ProbeEventPublisher publisher;
    public SendToHdConsumer(RabbitInitializer initializer, OrderServiceFeign orderServiceFeign, ProbeEventPublisher publisher) {
        this.orderServiceFeign = orderServiceFeign;
        this.publisher = publisher;
        //HealthyGoodQueue.DelayQueue.SEND_TO_HD.init(initializer);
    }

    /**
     * 配送时间内发送到基础订单修改为配送状态
     *
     * @param orderCode
     */
    @RabbitHandler
    @RabbitListener(queues = HealthyGoodQueue.DelayQueue.SEND_TO_HD_CONSUMER)
    public void sendToHd(String orderCode) {
        log.info("配送时间内发送到基础订单修改为配送状态:{}",orderCode);
        try {
            ResponseEntity responseEntity = orderServiceFeign.sendOrderToHd(orderCode);
            if (Objects.isNull(responseEntity)) {
                publisher.mqConsumerException(new NullPointerException(), Maps.of("message", "发送海鼎失败,基础服务未响应"));
            } else if (responseEntity.getStatusCode().isError()) {
                publisher.mqConsumerException(new ServerException("调用远程订单发送海鼎失败"), Maps.of("message", "发送海鼎失败,基础服务错误" + responseEntity.getBody()));
            }
        } catch (Exception e) {
            // 异常、队列消息
            publisher.mqConsumerException(e, Maps.of("message", "发送海鼎失败"));
        }
    }
}
