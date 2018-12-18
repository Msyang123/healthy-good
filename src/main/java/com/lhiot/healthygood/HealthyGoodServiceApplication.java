package com.lhiot.healthygood;

import com.leon.microx.util.Maps;
import com.lhiot.healthygood.service.customplan.CustomOrderService;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableAsync
public class HealthyGoodServiceApplication {

    @Bean(CustomOrderService.CUSTOM_PLAN_TASK_EXCHANGE)
    public DirectExchange directExchange() {
        return new DirectExchange(CustomOrderService.CUSTOM_PLAN_TASK_EXCHANGE, true, false);
    }

    @Bean(CustomOrderService.CUSTOM_PLAN_TASK_DLX)
    public Queue dlxQueue(@Qualifier(CustomOrderService.CUSTOM_PLAN_TASK_EXCHANGE) DirectExchange exchange) {
        return new Queue(CustomOrderService.CUSTOM_PLAN_TASK_DLX, true, false, false,
                Maps.of("x-dead-letter-exchange", exchange.getName(), "x-dead-letter-routing-key", CustomOrderService.CUSTOM_PLAN_TASK_RECEIVE)
        );
    }

    @Bean(CustomOrderService.CUSTOM_PLAN_TASK_RECEIVE)
    public Queue receiveQueue() {
        return new Queue(CustomOrderService.CUSTOM_PLAN_TASK_RECEIVE, true, false, false);
    }

    @Bean
    public Binding dlxBind(@Qualifier(CustomOrderService.CUSTOM_PLAN_TASK_EXCHANGE) DirectExchange exchange, @Qualifier(CustomOrderService.CUSTOM_PLAN_TASK_DLX) Queue dlxQueue) {
        return BindingBuilder.bind(dlxQueue).to(exchange).with(dlxQueue.getName());
    }

    @Bean
    public Binding receiveBind(@Qualifier(CustomOrderService.CUSTOM_PLAN_TASK_EXCHANGE) DirectExchange exchange, @Qualifier(CustomOrderService.CUSTOM_PLAN_TASK_RECEIVE) Queue receiveQueue) {
        return BindingBuilder.bind(receiveQueue).to(exchange).with(receiveQueue.getName());
    }

    /*********************************************************/
    @Bean(CustomOrderService.CUSTOM_PLAN_ORDER_TASK_EXCHANGE)
    public DirectExchange directOrderExchange() {
        return new DirectExchange(CustomOrderService.CUSTOM_PLAN_ORDER_TASK_EXCHANGE, true, false);
    }

    @Bean(CustomOrderService.CUSTOM_PLAN_ORDER_TASK_DLX)
    public Queue dlxOrderQueue(@Qualifier(CustomOrderService.CUSTOM_PLAN_ORDER_TASK_EXCHANGE) DirectExchange exchange) {
        return new Queue(CustomOrderService.CUSTOM_PLAN_ORDER_TASK_DLX, true, false, false,
                Maps.of("x-dead-letter-exchange", exchange.getName(), "x-dead-letter-routing-key", CustomOrderService.CUSTOM_PLAN_ORDER_TASK_RECEIVE)
        );
    }

    @Bean(CustomOrderService.CUSTOM_PLAN_ORDER_TASK_RECEIVE)
    public Queue receiveOrderQueue() {
        return new Queue(CustomOrderService.CUSTOM_PLAN_ORDER_TASK_RECEIVE, true, false, false);
    }

    @Bean
    public Binding dlxOrderBind(@Qualifier(CustomOrderService.CUSTOM_PLAN_ORDER_TASK_EXCHANGE) DirectExchange exchange, @Qualifier(CustomOrderService.CUSTOM_PLAN_ORDER_TASK_DLX) Queue dlxQueue) {
        return BindingBuilder.bind(dlxQueue).to(exchange).with(dlxQueue.getName());
    }

    @Bean
    public Binding receiveOrderBind(@Qualifier(CustomOrderService.CUSTOM_PLAN_ORDER_TASK_EXCHANGE) DirectExchange exchange, @Qualifier(CustomOrderService.CUSTOM_PLAN_ORDER_TASK_RECEIVE) Queue receiveQueue) {
        return BindingBuilder.bind(receiveQueue).to(exchange).with(receiveQueue.getName());
    }

    public static void main(String[] args) {
        SpringApplication.run(HealthyGoodServiceApplication.class, args);
    }
}
