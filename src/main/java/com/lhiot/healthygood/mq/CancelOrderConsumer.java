package com.lhiot.healthygood.mq;

import com.leon.microx.amqp.RabbitInitializer;
import com.leon.microx.probe.collector.ProbeEventPublisher;
import com.leon.microx.util.Maps;
import com.leon.microx.web.result.Tips;
import com.lhiot.healthygood.feign.OrderServiceFeign;
import com.lhiot.healthygood.feign.model.OrderDetailResult;
import com.lhiot.healthygood.feign.type.OrderStatus;
import com.lhiot.healthygood.util.FeginResponseTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.rmi.ServerException;
import java.util.Objects;

/**
 * 取消未支付普通订单
 */
@Component
@Slf4j
public class CancelOrderConsumer {

    private final OrderServiceFeign orderServiceFeign;
    private final ProbeEventPublisher publisher;
    public CancelOrderConsumer(RabbitInitializer initializer, OrderServiceFeign orderServiceFeign, ProbeEventPublisher publisher) {
        this.orderServiceFeign = orderServiceFeign;
        this.publisher = publisher;
        HealthyGoodQueue.DelayQueue.CANCEL_ORDER.init(initializer);
    }

    /**
     * 取消未支付普通订单 订单状态修改为已失效
     *
     * @param orderCode
     */
    @RabbitHandler
    @RabbitListener(queues = HealthyGoodQueue.DelayQueue.CANCEL_ORDER_CONSUMER)
    public void cancelUnPayOrder(String orderCode) {
        try {
            ResponseEntity<OrderDetailResult> orderDetailResultResponseEntity = orderServiceFeign.orderDetail(orderCode,false,false);
            Tips<OrderDetailResult> orderDetailResultTips = FeginResponseTools.convertResponse(orderDetailResultResponseEntity);
            if(orderDetailResultTips.err()){
                log.warn("订单状态修改为已失效未找到订单或失败",orderDetailResultTips);
                return;
            }
            if(Objects.equals(OrderStatus.WAIT_PAYMENT,orderDetailResultTips.getData().getStatus())){
                ResponseEntity responseEntity = orderServiceFeign.updateOrderStatus(orderCode, OrderStatus.FAILURE);
                if (Objects.isNull(responseEntity)) {
                    publisher.mqConsumerException(new NullPointerException(), Maps.of("message", "订单状态修改为已失效,基础服务未响应"));
                } else if (responseEntity.getStatusCode().isError()) {
                    publisher.mqConsumerException(new ServerException("调用远程订单发送海鼎失败"), Maps.of("message", "订单状态修改为已失效,基础服务错误" + responseEntity.getBody()));
                }
                log.info("普通订单超过30分钟未支付，已取消{}",orderCode);
            }else{
                log.info("普通订单超过30分钟未支付检查，已支付{}",orderCode);
            }
        } catch (Exception e) {
            // 异常、队列消息
            publisher.mqConsumerException(e, Maps.of("message", "订单状态修改为已失效失败"));
        }
    }
}
