package com.lhiot.healthygood.mq;

import com.leon.microx.probe.collector.ProbeEventPublisher;
import com.leon.microx.util.Maps;
import com.lhiot.healthygood.wechat.Token;
import com.lhiot.healthygood.wechat.WeChatUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * wechat 定时刷新access_token
 */
@Component
@Slf4j
public class RefreshAccessTokenConsumer {
    private final static String PREFIX_REDIS = "lhiot:healthy_good:";//
    private final ProbeEventPublisher publisher;
    private final RabbitTemplate rabbitTemplate;
    private final WeChatUtil weChatUtil;
    private RedissonClient redissonClient;

    public RefreshAccessTokenConsumer(ProbeEventPublisher publisher, RabbitTemplate rabbitTemplate, WeChatUtil weChatUtil, RedissonClient redissonClient) {
        this.publisher = publisher;
        this.rabbitTemplate = rabbitTemplate;
        this.weChatUtil = weChatUtil;
        this.redissonClient = redissonClient;
    }
    @RabbitHandler
    @RabbitListener(queues = HealthyGoodQueue.DelayQueue.CACHE_NEW_ACCESS_TOKEN)
    public void refreshAccessToken() {
        try {
            RMapCache<String,String> cache=  redissonClient.getMapCache(PREFIX_REDIS+"acce" +
                    "" +
                    "ssToken");
            //获取access_token(2小时) 缓存
            String accessToken=cache.get("accessToken");
            if(Objects.isNull(accessToken)){
                Token token = weChatUtil.getToken();
                accessToken=token.getAccessToken();
                cache.put("accessToken",accessToken,token.getExpiresIn(), TimeUnit.MILLISECONDS);
            }
        }catch (Exception e){
            // 异常、队列消息
            publisher.mqConsumerException(e, Maps.of("message", "wechat 刷新access_token"));
        }

    }
}
