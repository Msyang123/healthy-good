package com.lhiot.healthygood.mq;

import com.leon.microx.amqp.RabbitInitializer;
import com.leon.microx.probe.collector.ProbeEventPublisher;
import com.leon.microx.util.Maps;
import com.leon.microx.web.result.Tips;
import com.lhiot.healthygood.config.HealthyGoodConfig;
import com.lhiot.healthygood.domain.customplan.CustomOrder;
import com.lhiot.healthygood.domain.customplan.CustomOrderDelivery;
import com.lhiot.healthygood.feign.PaymentServiceFeign;
import com.lhiot.healthygood.feign.model.Record;
import com.lhiot.healthygood.feign.model.RefundModel;
import com.lhiot.healthygood.mapper.customplan.CustomOrderDeliveryMapper;
import com.lhiot.healthygood.service.customplan.CustomOrderService;
import com.lhiot.healthygood.type.CustomOrderStatus;
import com.lhiot.healthygood.util.FeginResponseTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 定制订单超过最后提取时间自动处理消费类
 */
@Component
@Slf4j
public class LastExtractionConsumer {

    private final CustomOrderService customOrderService;
    private final ProbeEventPublisher publisher;
    private final PaymentServiceFeign paymentServiceFeign;
    private final HealthyGoodConfig.WechatPayConfig wechatPayConfig;
    public LastExtractionConsumer(RabbitInitializer initializer, CustomOrderService customOrderService, ProbeEventPublisher publisher, PaymentServiceFeign paymentServiceFeign,HealthyGoodConfig healthyGoodConfig) {
        this.customOrderService = customOrderService;
        this.publisher = publisher;
        this.paymentServiceFeign = paymentServiceFeign;
        this.wechatPayConfig = healthyGoodConfig.getWechatPay();
        //HealthyGoodQueue.DelayQueue.LAST_EXTRACTION.init(initializer);
    }

    /**
     * 最后提取日期' 需要到期就原路退回到用户账户上（退款金额为定制单个订单均价*剩余未提取数量）
     *
     * @param customOrderCode
     */
    @RabbitHandler
    @RabbitListener(queues = HealthyGoodQueue.DelayQueue.LAST_EXTRACTION_CONSUMER)
    public void endExtractionConsumer(String customOrderCode) {
        CustomOrder customOrder = customOrderService.selectByCode(customOrderCode);
        if (Objects.isNull(customOrder) || customOrder.getRemainingQty() <= 0
                || Objects.equals(customOrder.getStatus(), CustomOrderStatus.WAIT_PAYMENT)
                || Objects.equals(customOrder.getStatus(), CustomOrderStatus.INVALID)
                || Objects.equals(customOrder.getStatus(), CustomOrderStatus.FINISHED)) {
            log.info("定制最后提取日期消费端,定制订单不存在或者定制订单剩余次数为0或者定制订单非暂停和恢复状态{},{}", customOrderCode, customOrder);
            return;
        }
        try {
            log.info("查询支付中心支付信息{}",customOrder.getPayId());
            ResponseEntity<Record> payLogResponse = paymentServiceFeign.one(customOrder.getPayId());
            Tips<Record> payLogTips = FeginResponseTools.convertResponse(payLogResponse);
            if (payLogTips.err()){
                log.warn("定制最后提取日期消费端 未找到定制订单支付记录{}", customOrder.getPayId());
                return;
            }
            //平均价计算剩余退款
            long refundFee =(long)(customOrder.getPrice()*customOrder.getRemainingQty()/customOrder.getTotalQty());
            RefundModel refundModel=new RefundModel();
            refundModel.setFee(refundFee);
            refundModel.setNotifyUrl(wechatPayConfig.getCustomplanRefundCallbackUrl());//定制退款回调
            refundModel.setReason("定制订单超过最后提取日期");
            ResponseEntity responseEntity = paymentServiceFeign.refund(customOrder.getPayId(),refundModel);
            log.info("定制最后提取日期消费端 发送退款申请{}",responseEntity);
        } catch (Exception e) {
            // 异常、队列消息
            publisher.mqConsumerException(e, Maps.of("message", "最后提取日期操作退款消费失败"));
        }
    }
}
