package com.lhiot.healthygood.mq;

import com.leon.microx.amqp.RabbitInitializer;
import lombok.Getter;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.Serializable;

public interface HealthyGoodQueue {

    String getExchange();

    String getDlx();

    String getReceive();

    default void init(RabbitInitializer initializer) {
        initializer.delay(this.getExchange(), this.getDlx(), this.getReceive());
    }

    default void send(RabbitTemplate delegate, Serializable data, long delay) {
        delegate.convertAndSend(this.getExchange(), this.getDlx(), data, message -> {
            message.getMessageProperties().setExpiration(String.valueOf(delay));
            return message;
        });
    }

    enum DelayQueue implements HealthyGoodQueue {

        AUTO_EXTRACTION("healthy-good.custom-order.autoExtraction.exchange", "healthy-good.custom-order.autoExtraction.dlx", DelayQueue.AUTO_EXTRACTION_CONSUMER),
        LAST_EXTRACTION("healthy-good.custom-order.lastExtraction.exchange", "healthy-good.custom-order.lastExtraction.-dlx", DelayQueue.LAST_EXTRACTION_CONSUMER),
        UPDATE_CUSTOM_ORDER_STATUS("healthy-good.custom-order.status.exchange", "healthy-good.custom-order.status.dlx", DelayQueue.UPDATE_CUSTOM_ORDER_STATUS_CONSUMER),
        SEND_TO_HD("healthy-good.order.sendToHd.exchange", "healthy-good.order.sendToHd.dlx", DelayQueue.SEND_TO_HD_CONSUMER),
        CANCEL_ORDER("healthy-good.order.cancel.exchange", "healthy-good.order.cancel.dlx", DelayQueue.CANCEL_ORDER_CONSUMER),
        CANCEL_CUSTOM_ORDER("healthy-good.custom-order.cancel.exchange", "healthy-good.custom-order.cancel.dlx", DelayQueue.CANCEL_CUSTOM_ORDER_CONSUMER),
        ;
        public static final String AUTO_EXTRACTION_CONSUMER = "healthy-good.custom-order.autoExtraction.receive";//自动提货消费端队列名
        public static final String LAST_EXTRACTION_CONSUMER = "healthy-good.custom-order.lastExtraction.receive";//定制订单到达最后提取时间自动退款处理消费端队列名
        public static final String UPDATE_CUSTOM_ORDER_STATUS_CONSUMER = "healthy-good.custom-order.status.receive";//依据配送暂停设置修改定制订单暂停/恢复状态
        public static final String SEND_TO_HD_CONSUMER = "healthy-good.order.sendToHd.receive";//延迟发送到海鼎
        public static final String CANCEL_ORDER_CONSUMER ="healthy-good.order.cancel.receive";//三十分钟未支付普通订单取消
        public static final String CANCEL_CUSTOM_ORDER_CONSUMER ="healthy-good.custom-order.cancel.receive";//三十分钟未支付定制订单取消


        @Getter
        private final String exchange;//信道
        @Getter
        private final String dlx;//死信队列
        @Getter
        private final String receive;//消费队列

        DelayQueue(String exchange, String dlx, String receive) {
            this.exchange = exchange;
            this.dlx = dlx;
            this.receive = receive;
        }
    }
}
