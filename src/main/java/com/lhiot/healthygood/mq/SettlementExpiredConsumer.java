package com.lhiot.healthygood.mq;

import com.leon.microx.probe.collector.ProbeEventPublisher;
import com.leon.microx.util.Maps;
import com.leon.microx.web.result.Pages;
import com.lhiot.healthygood.domain.doctor.SettlementApplication;
import com.lhiot.healthygood.service.doctor.SettlementApplicationService;
import com.lhiot.healthygood.type.SettlementStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 薪资结算申请3天未处理将结算状态自动改为已过期的消费类
 * @author hufan created in 2019/1/10 16:08
 **/
@Slf4j
@Component
public class SettlementExpiredConsumer {
    private final ProbeEventPublisher publisher;
    private final RabbitTemplate rabbitTemplate;
    private final SettlementApplicationService settlementApplicationService;

    public SettlementExpiredConsumer(ProbeEventPublisher publisher, RabbitTemplate rabbitTemplate, SettlementApplicationService settlementApplicationService) {
        this.publisher = publisher;
        this.rabbitTemplate = rabbitTemplate;
        this.settlementApplicationService = settlementApplicationService;
    }

    /**
     * 薪资结算申请3天未处理将结算状态自动改为已过期
     */
    @RabbitHandler
    @RabbitListener(queues = HealthyGoodQueue.DelayQueue.SETTLEMENT_EXPIRED_CONSUMER)
    public void expiredStatusJob() {
        log.debug("薪资结算3天未处理改为已过期定时任务处理\t");

        try {
            // 每月结算时间为15日-17日，当前时间为每个月18日-20日进行定时任务处理
            LocalDate localDate = LocalDate.now();
            //每月18号
            LocalDate eighteenth = LocalDate.of(localDate.getYear(), localDate.getMonth(), 18);
            //每月20号
            LocalDate twentieth = LocalDate.of(localDate.getYear(), localDate.getMonth(), 20);
            if (localDate.isBefore(eighteenth) || localDate.isAfter(twentieth)) {
                return ;
            }
            //查询所有 未处理数据
            SettlementApplication param = new SettlementApplication();
            param.setSettlementStatus(SettlementStatus.UNSETTLED);
            Pages<SettlementApplication> pages = settlementApplicationService.pageList(param);
            if (CollectionUtils.isEmpty(pages.getArray())) {
                return;
            }
            //批量修改ids
            List<Long> ids = null;
            //获取创建时间
            Date today = new Date();
            //筛选过期数据集合
            ids = pages.getArray().stream().filter(settlementApplication ->
                    //当前时间减去创建时间大于3天未处理 ,修改状态为已过期
                today.getTime() - settlementApplication.getCreateAt().getTime() > (1 * 24 * 60 * 60 * 1000) * 3)
//                    today.getTime() - settlementApplication.getCreateAt().getTime() > (3 * 60 * 1000)) //测试3分钟
                    .map(settlementApplication -> settlementApplication.getId()).collect(Collectors.toList());
            //修改状态
            if (!CollectionUtils.isEmpty(ids)) {
                settlementApplicationService.updateExpiredStatus(ids);
            }
        } catch (Exception e) {
            // 异常、队列消息
            publisher.mqConsumerException(e, Maps.of("message", "薪资结算申请3天未处理将结算状态自动改为已过期"));
        }
        //每天循环调用一次
//        HealthyGoodQueue.DelayQueue.UPDATE_CUSTOM_ORDER_STATUS.send(rabbitTemplate,"nothing",5 * 60 * 1000); // 测试5分钟调一次
        HealthyGoodQueue.DelayQueue.UPDATE_CUSTOM_ORDER_STATUS.send(rabbitTemplate,"nothing",1 * 24 * 60 * 60 * 1000 * 1);


    }
}