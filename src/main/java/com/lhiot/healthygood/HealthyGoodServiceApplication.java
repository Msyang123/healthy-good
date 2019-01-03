package com.lhiot.healthygood;

import com.leon.microx.amqp.RabbitInitRunner;
import com.lhiot.healthygood.mq.HealthyGoodQueue;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

import java.util.stream.Stream;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableAsync
public class HealthyGoodServiceApplication {

    @Bean
    public RabbitInitRunner runner() {
        return initializer -> Stream.of(HealthyGoodQueue.DelayQueue.values())
                .forEach(delayQueue -> delayQueue.init(initializer));
    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

    public static void main(String[] args) {
        SpringApplication.run(HealthyGoodServiceApplication.class, args);
    }

}
