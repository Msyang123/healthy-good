package com.lhiot.healthygood.util;

import com.leon.microx.probe.collector.ProbeEventPublisher;
import com.leon.microx.util.Maps;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class Constants {
    public static final String CUSTOM_PLAN_TASK_EXCHANGE = "healthy-good-custom-plan-task-exchange";
    public static final String CUSTOM_PLAN_TASK_DLX = "healthy-good-custom-plan-task-dlx";
    public static final String CUSTOM_PLAN_TASK_RECEIVE = "healthy-good-custom-plan-task-receive";


    private ProbeEventPublisher publisher;
    private RabbitTemplate rabbitTemplate;

    public Constants(ProbeEventPublisher publisher, RabbitTemplate rabbitTemplate) {
        this.publisher = publisher;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void schedule(String planId) {
        long interval = 10_000L;//时间依据用户的时间来做
        rabbitTemplate.convertAndSend(CUSTOM_PLAN_TASK_EXCHANGE, CUSTOM_PLAN_TASK_DLX, planId, message -> {
            message.getMessageProperties().setExpiration(String.valueOf(interval));
            return message;
        });
    }

    @RabbitHandler
    @RabbitListener(queues = CUSTOM_PLAN_TASK_RECEIVE)
    public void deliver(String planId) {
        try {
            // 检查是否可以配送
            // service.checked(planId)
            //配送次数完成 终结掉

            // 发配送
            // service.sent(planId)

            // 计数
            // service.counter(planId).add()

            // 配送次数 大于等总次数 改本定制计划
            // service.ifEnd(planId)



            // 下一次配送
            this.schedule(planId);
        } catch (Exception e) {
            // 异常、队列消息保存到数据库
            publisher.mqConsumerException(e, Maps.of("message", "配送失败"));
        }
    }
}
