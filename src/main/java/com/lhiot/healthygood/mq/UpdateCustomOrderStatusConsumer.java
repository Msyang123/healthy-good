package com.lhiot.healthygood.mq;

import com.leon.microx.amqp.RabbitInitializer;
import com.leon.microx.probe.collector.ProbeEventPublisher;
import com.leon.microx.util.Maps;
import com.lhiot.healthygood.domain.customplan.CustomOrder;
import com.lhiot.healthygood.domain.customplan.CustomOrderPause;
import com.lhiot.healthygood.mapper.customplan.CustomOrderPauseMapper;
import com.lhiot.healthygood.service.customplan.CustomOrderService;
import com.lhiot.healthygood.type.CustomOrderStatus;
import com.lhiot.healthygood.type.OperStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 依据配送暂停设置修改定制订单状态为暂停状态和恢复状态消费类
 */
@Component
@Slf4j
public class UpdateCustomOrderStatusConsumer {

    private final CustomOrderPauseMapper customOrderPauseMapper;
    private final CustomOrderService customOrderService;
    private final ProbeEventPublisher publisher;
    private final RabbitTemplate rabbitTemplate;
    public UpdateCustomOrderStatusConsumer(RabbitInitializer initializer, CustomOrderPauseMapper customOrderPauseMapper, CustomOrderService customOrderService, ProbeEventPublisher publisher, RabbitTemplate rabbitTemplate) {
        this.customOrderPauseMapper = customOrderPauseMapper;
        this.customOrderService = customOrderService;
        this.publisher = publisher;
        this.rabbitTemplate = rabbitTemplate;
        //HealthyGoodQueue.DelayQueue.UPDATE_CUSTOM_ORDER_STATUS.init(initializer);
    }

    /**
     * 依据配送暂停设置修改定制订单状态为暂停状态和恢复状态
     *
     */
    @RabbitHandler
    @RabbitListener(queues = HealthyGoodQueue.DelayQueue.UPDATE_CUSTOM_ORDER_STATUS_CONSUMER)
    public void updateStatus(String message) {
        try {
            List<CustomOrderPause> customOrderPauseList = customOrderPauseMapper.selectAll();
            if(Objects.isNull(customOrderPauseList)){
                log.info("依据配送暂停设置修改定制订单状态为暂停状态和恢复状态,没有符合条件的暂停设置");
                return;
            }
            Date current =new Date();
            customOrderPauseList.forEach(item->{
                //当前时间是在暂停时间内
                if(current.after(item.getPauseBeginAt())&& current.before(item.getPauseEndAt())){
                    CustomOrder customOrder =new CustomOrder();
                    customOrder.setCustomOrderCode(item.getCustomOrderCode());

                    if(Objects.equals(item.getOperStatus(), OperStatus.PAUSE)){
                        //设置定制订单为暂停
                        customOrder.setStatus(CustomOrderStatus.PAUSE_DELIVERY);
                    }else if(Objects.equals(item.getOperStatus(), OperStatus.RECOVERY)){
                        //设置定制订单为恢复
                        customOrder.setStatus(CustomOrderStatus.CUSTOMING);
                    }
                    customOrderService.updateByCode(customOrder);
                }else if(current.after(item.getPauseEndAt())){
                    //当前时间大于实际暂停结束时间，那么就自动恢复
                    CustomOrder customOrder =new CustomOrder();
                    customOrder.setCustomOrderCode(item.getCustomOrderCode());
                    //设置定制订单为恢复
                    customOrder.setStatus(CustomOrderStatus.CUSTOMING);
                    customOrderService.updateByCode(customOrder);
                    //定制设置已经过了设置的时间了，不在做恢复状态修改
                }
            });
        } catch (Exception e) {
            // 异常、队列消息
            publisher.mqConsumerException(e, Maps.of("message", "依据配送暂停设置修改定制订单状态为暂停状态和恢复状态"));
        }
        //每五分钟循环调用一次
        HealthyGoodQueue.DelayQueue.UPDATE_CUSTOM_ORDER_STATUS.send(rabbitTemplate,"nothing",5*60*1000);
    }
}
