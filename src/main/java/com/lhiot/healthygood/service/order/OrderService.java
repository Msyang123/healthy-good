package com.lhiot.healthygood.service.order;

import com.leon.microx.util.Jackson;
import com.leon.microx.util.Position;
import com.leon.microx.util.StringUtils;
import com.leon.microx.web.result.Tips;
import com.lhiot.healthygood.config.HealthyGoodConfig;
import com.lhiot.healthygood.domain.customplan.CustomOrder;
import com.lhiot.healthygood.domain.customplan.CustomOrderDelivery;
import com.lhiot.healthygood.feign.DeliverServiceFeign;
import com.lhiot.healthygood.feign.OrderServiceFeign;
import com.lhiot.healthygood.feign.model.*;
import com.lhiot.healthygood.feign.type.*;
import com.lhiot.healthygood.mapper.customplan.CustomOrderDeliveryMapper;
import com.lhiot.healthygood.mapper.customplan.CustomOrderMapper;
import com.lhiot.healthygood.mq.HealthyGoodQueue;
import com.lhiot.healthygood.service.common.CommonService;
import com.lhiot.healthygood.type.CustomOrderDeliveryStatus;
import com.lhiot.healthygood.type.ReceivingWay;
import com.lhiot.healthygood.util.FeginResponseTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Description:服务类
 *
 * @author yangjiawen
 */
@Service
@Transactional
@Slf4j
public class OrderService {

    private final OrderServiceFeign orderServiceFeign;
    private final DeliverServiceFeign deliverServiceFeign;
    private final HealthyGoodConfig.DeliverConfig deliverConfig;
    private final HealthyGoodConfig.WechatPayConfig wechatPayConfig;
    private final CommonService commonService;
    private final CustomOrderMapper customOrderMapper;
    private final CustomOrderDeliveryMapper customOrderDeliveryMapper;
    private final RabbitTemplate rabbitTemplate;


    @Autowired
    public OrderService(OrderServiceFeign orderServiceFeign,
                        DeliverServiceFeign deliverServiceFeign,
                        HealthyGoodConfig healthyGoodConfig, CommonService commonService,
                        CustomOrderMapper customOrderMapper, CustomOrderDeliveryMapper customOrderDeliveryMapper, RabbitTemplate rabbitTemplate) {

        this.orderServiceFeign = orderServiceFeign;
        this.deliverServiceFeign = deliverServiceFeign;
        this.deliverConfig = healthyGoodConfig.getDeliver();
        this.wechatPayConfig= healthyGoodConfig.getWechatPay();
        this.commonService = commonService;
        this.customOrderMapper = customOrderMapper;
        this.customOrderDeliveryMapper = customOrderDeliveryMapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    //处理海鼎回调
    public Tips hdCallbackDeal(@RequestBody Map<String, Object> map) {
        Map<String, String> contentMap = (Map<String, String>) map.get("content");

        log.info("content = " + contentMap.toString());
        String orderCode = contentMap.get("front_order_id");
        //查询订单信息
        ResponseEntity<OrderDetailResult> orderDetailResultResponseEntity = orderServiceFeign.orderDetail(orderCode, true, false);
        if (Objects.isNull(orderDetailResultResponseEntity)
                || orderDetailResultResponseEntity.getStatusCode().isError()) {
            return Tips.warn(String.valueOf(orderDetailResultResponseEntity.getBody()));
        }
        OrderDetailResult orderDetailResult = orderDetailResultResponseEntity.getBody();
        // 所有订单推送类消息
        if ("order".equals(map.get("group"))) {
            // 订单备货
            if ("order.shipped".equals(map.get("topic"))) {
                log.info("订单备货回调********");
                if (!Objects.equals(orderDetailResult.getStatus(), OrderStatus.SEND_OUTING)) {
                    //如果已经处理此订单信息，就不重复处理
                    return Tips.info(orderCode);
                }
                //如果送货上门 发送订单到配送中心

                //门店自提 此处不做处理，等待孚利购推送订单出店接口数据再修改订单完成
                if (Objects.equals(orderDetailResult.getReceivingWay(), ReceivingWay.TO_THE_STORE)) {
                    //调用基础服务修改为已发货状态baseUrl
                    orderServiceFeign.updateOrderStatus(orderDetailResult.getCode(), OrderStatus.RECEIVED);
                } else {
                    //发送到配送(达达)
                    DeliveryParam deliveryParam = new DeliveryParam();
                    deliveryParam.setApplicationType(ApplicationType.HEALTH_GOOD);
                    deliveryParam.setBackUrl(StringUtils.format(deliverConfig.getBackUrl(), deliverConfig.getType()));//配置回调
                    deliveryParam.setCoordinate(CoordinateSystem.AMAP);

                    deliveryParam.setDeliverTime(Jackson.object(orderDetailResult.getDeliverAt(), DeliverTime.class));
                    deliveryParam.setDeliveryType(DeliverType.valueOf(deliverConfig.getType()));
                    Position.GCJ02 position = commonService.getPositionFromAddres(orderDetailResult.getAddress());//地址转经纬度
                    if (Objects.isNull(position)) {
                        log.error("查询收货地址经纬度信息失败{}", orderDetailResult.getAddress());
                        deliveryParam.setLat(0.0);
                        deliveryParam.setLng(0.0);
                    } else {
                        deliveryParam.setLat(position.getLatitude());
                        deliveryParam.setLng(position.getLongitude());
                    }
                    ResponseEntity updateOrderToDeliveryResponse = orderServiceFeign.updateOrderToDelivery(orderDetailResult.getCode(), deliveryParam);

                    if (Objects.nonNull(updateOrderToDeliveryResponse) && updateOrderToDeliveryResponse.getStatusCode().is2xxSuccessful()) {
                        log.info("调用基础服务修改为已发货状态正常{}", orderCode);
                    } else {
                        log.error("调用基础服务修改为已发货状态错误{}", orderCode);
                    }
                }
                return Tips.info(orderCode);
            } else if ("return.received".equals(map.get("topic"))) {
                log.info("订单退货回调*********{}", orderDetailResult);
                if (Objects.equals(OrderStatus.RETURNING, orderDetailResult.getStatus())) {
                    log.info("给用户退款", orderDetailResult);

                    if (Objects.equals(OrderType.CUSTOM, orderDetailResult.getOrderType())) {
                        //定制订单 只退用户剩余次数，不退款
                        CustomOrderDelivery customOrderDelivery = customOrderDeliveryMapper.selectOrderCode(orderCode);
                        if (Objects.isNull(customOrderDelivery)) {
                            log.error("订单退货回调,找不到提取的定制配送记录,{}", orderCode);
                            return Tips.warn("订单退货回调,找不到提取的定制配送记录");
                        }

                        CustomOrder customOrder = new CustomOrder();
                        customOrder.setId(customOrderDelivery.getCustomOrderId());
                        customOrder.setRemainingQtyAdd(1);//退还一次剩余
                        customOrderMapper.updateById(customOrder);
                        CustomOrderDelivery updateCustomOrderDelivery = new CustomOrderDelivery();
                        updateCustomOrderDelivery.setOrderCode(orderCode);
                        updateCustomOrderDelivery.setDeliveryStatus(CustomOrderDeliveryStatus.ALREADY_RETURN);//退货完成
                        //修改当前配送记录为已退货
                        customOrderDeliveryMapper.updateByOrderCode(updateCustomOrderDelivery);
                        //通知基础服务已经海鼎回调退货到门店
                        ResponseEntity notPayedRefundResponse = orderServiceFeign.notPayedRefund(orderCode, NotPayRefundWay.STOCKING);
                        log.info("定制订单发送基础服务订单实际未支付退货（无需退款）{}", notPayedRefundResponse);
                    } else {
                        //普通订单
                        ReturnOrderParam returnOrderParam =new ReturnOrderParam();
                        returnOrderParam.setNotifyUrl(wechatPayConfig.getOrderRefundCallbackUrl());
                        Tips refundOrderTips = FeginResponseTools.convertResponse(orderServiceFeign.refundOrder(orderDetailResult.getCode(), returnOrderParam));//此处为用户依据申请了退货了，海鼎回调中不需要再告知基础服务退货列表
                        if (refundOrderTips.err()) {
                            log.error("调用基础服务 refundOrder失败{}", orderDetailResult);
                            return Tips.warn(String.valueOf(orderDetailResultResponseEntity.getBody()));
                        }
                        //发起用户退款 等待用户退款回调然后发送orderServiceFeign.refundConfirmation
                    }
                }
                return Tips.info(orderCode);
            } else {
                log.info("hd other group message= " + map.get("group"));
            }
        }
        return Tips.warn(String.valueOf(orderDetailResultResponseEntity.getBody()));
    }

    public Tips deliverCallbackDeal(Map<String, Object> param) {
        //修改配送单状态 如果配送完成 修改订单状态为已完成
        Map<String, String> stringParams = Optional.ofNullable(param).map(
                (v) -> {
                    Map<String, String> params = v.entrySet().stream()
                            .filter((e) -> StringUtils.isNotEmpty(e.getValue()))
                            .collect(Collectors.toMap(
                                    (e) -> e.getKey(),
                                    (e) -> Objects.isNull(e.getValue()) ? null : e.getValue().toString()
                            ));
                    return params;
                }
        ).orElse(null);
        //验证签名
        ResponseEntity<Tips> backSignature = deliverServiceFeign.backSignature(DeliverType.valueOf(deliverConfig.getType()), stringParams);

        if (Objects.nonNull(backSignature) && backSignature.getStatusCode().is2xxSuccessful()) {
            log.debug("配送签名回调验证结果:{}", backSignature.getBody());
            String orderCode = stringParams.get("order_id");

            switch ((int) param.get("order_status")) {
                case 1:
                    deliverServiceFeign.update(orderCode, new DeliverUpdate(orderCode, DeliverStatus.UNRECEIVE, null, null, null));
                    return Tips.info("配送待接单");
                case 2:
                    //调用基础服务修改为配送中状态
                    orderServiceFeign.updateOrderStatus(orderCode, OrderStatus.DISPATCHING);
                    deliverServiceFeign.update(orderCode, new DeliverUpdate(orderCode, DeliverStatus.WAIT_GET, stringParams.get("dm_name"), stringParams.get("dm_mobile"), null));
                    return Tips.info("配送待取货");
                case 3:
                    deliverServiceFeign.update(orderCode, new DeliverUpdate(orderCode, DeliverStatus.DELIVERING, null, null, null));
                    return Tips.info("配送配送中");
                case 4:
                    deliverServiceFeign.update(orderCode, new DeliverUpdate(orderCode, DeliverStatus.DONE, null, null, null));
                    orderServiceFeign.updateOrderStatus(orderCode, OrderStatus.RECEIVED);
                    ResponseEntity<OrderDetailResult> orderDetailResultResponseEntity = orderServiceFeign.orderDetail(orderCode, false, false);
                    Tips<OrderDetailResult> orderDetailResultTips = FeginResponseTools.convertResponse(orderDetailResultResponseEntity);
                    if (orderDetailResultTips.succ()) {
                        //定制订单类型的订单
                        if (Objects.equals(OrderType.CUSTOM, orderDetailResultTips.getData().getOrderType())) {
                            CustomOrderDelivery updateCustomOrderDelivery = new CustomOrderDelivery();
                            updateCustomOrderDelivery.setOrderCode(orderCode);
                            updateCustomOrderDelivery.setDeliveryStatus(CustomOrderDeliveryStatus.RECEIVED);//定制订单已收货
                            updateCustomOrderDelivery.setRecevingTime(Date.from(Instant.now()));//配送时间
                            customOrderDeliveryMapper.updateByOrderCode(updateCustomOrderDelivery);
                        }
                    }
                    return Tips.info("配送配送完成");
                case 5:
                    deliverServiceFeign.update(orderCode, new DeliverUpdate(orderCode, DeliverStatus.FAILURE, null, null, "已取消"));
                    return Tips.info("配送已取消");
                case 7:
                    deliverServiceFeign.update(orderCode, new DeliverUpdate(orderCode, DeliverStatus.FAILURE, null, null, "已过期"));
                    return Tips.info("配送已过期");
                case 1000:
                    deliverServiceFeign.update(orderCode, new DeliverUpdate(orderCode, DeliverStatus.FAILURE, null, null, stringParams.get("cancel_reason")));
                    return Tips.info("配送失败");
                default:
                    break;
            }
        } else {
            log.error("配送回调验证签名不通过");
        }
        //直接不作处理
        return Tips.info("默认处理");
    }

    /**
     * 延迟发送海鼎
     *
     * @param orderCode   普通订单号
     * @param deliverTime 延迟发送时间
     */
    public void delaySendToHd(String orderCode, DeliverTime deliverTime) {
        //送货上门订单 本地mq延迟到配送时间发送海鼎
        LocalDateTime current = LocalDateTime.now();
        //计算配送的时间与当前时间的毫秒数，做为消费端处理配送时候的时间
        final Long interval = deliverTime.getStartTime().getTime() - current.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        //如果计算结果为负数，那么就只延迟1秒钟
        //延迟发送到海鼎
        HealthyGoodQueue.DelayQueue.SEND_TO_HD.send(rabbitTemplate, orderCode, (interval <= 0 ? 1000L : interval));
        log.info("创建订单提取延迟发送到海鼎:{},{}", orderCode, interval);
    }


}
