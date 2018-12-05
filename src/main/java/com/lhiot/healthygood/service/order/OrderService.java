package com.lhiot.healthygood.service.order;

import com.leon.microx.util.Calculator;
import com.leon.microx.util.StringUtils;
import com.leon.microx.web.result.Tips;
import com.lhiot.healthygood.config.HealthyGoodConfig;
import com.lhiot.healthygood.feign.BaseDataServiceFeign;
import com.lhiot.healthygood.feign.DeliverServiceFeign;
import com.lhiot.healthygood.feign.OrderServiceFeign;
import com.lhiot.healthygood.feign.model.*;
import com.lhiot.healthygood.feign.type.*;
import com.lhiot.healthygood.type.ReceivingWay;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Description:服务类
 *
 * @author yangjiawen
 * @date 2018/07/26
 */
@Service
@Transactional
@Slf4j
public class OrderService {

    private final OrderServiceFeign orderServiceFeign;
    private final DeliverServiceFeign deliverServiceFeign;
    private final BaseDataServiceFeign baseDataServiceFeign;
    private final HealthyGoodConfig.WechatPayConfig wechatPayConfig;
    private final HealthyGoodConfig.AliPayConfig aliPayConfig;
    private final HealthyGoodConfig.DeliverConfig deliverConfig;

    @Autowired
    public OrderService(OrderServiceFeign orderServiceFeign,
                        DeliverServiceFeign deliverServiceFeign,
                        BaseDataServiceFeign baseDataServiceFeign,
                        HealthyGoodConfig healthyGoodConfig) {

        this.orderServiceFeign = orderServiceFeign;
        this.deliverServiceFeign = deliverServiceFeign;
        this.baseDataServiceFeign = baseDataServiceFeign;
        this.wechatPayConfig = healthyGoodConfig.getWechatPay();
        this.aliPayConfig = healthyGoodConfig.getAliAay();
        this.deliverConfig = healthyGoodConfig.getDeliver();
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
            return Tips.of(HttpStatus.BAD_REQUEST, String.valueOf(orderDetailResultResponseEntity.getBody()));
        }
        OrderDetailResult orderDetailResult = orderDetailResultResponseEntity.getBody();
        // 所有订单推送类消息
        if ("order".equals(map.get("group"))) {
            // 订单备货
            if ("order.shipped".equals(map.get("topic"))) {
                log.info("订单备货回调********");
                if (!Objects.equals(orderDetailResult.getStatus(), OrderStatus.WAIT_SEND_OUT)) {
                    //如果已经处理此订单信息，就不重复处理
                    return Tips.of(HttpStatus.OK, orderCode);
                }
                //如果送货上门 发送订单到配送中心

                //门店自提 此处不做处理，等待孚利购推送订单出店接口数据再修改订单完成
                if (Objects.equals(orderDetailResult.getReceivingWay(), ReceivingWay.TO_THE_STORE)) {
                    //调用基础服务修改为已发货状态baseUrl
                    orderServiceFeign.updateOrderStatus(orderDetailResult.getCode(), OrderStatus.RECEIVED);
                } else {
                    //发送到配送(达达)
                    DeliverOrder deliverOrder = new DeliverOrder();
                    deliverOrder.setAddress(orderDetailResult.getAddress());
                    deliverOrder.setAmountPayable(orderDetailResult.getAmountPayable());
                    deliverOrder.setApplyType(ApplicationType.HEALTH_GOOD);
                    deliverOrder.setBackUrl(StringUtils.format(deliverConfig.getBackUrl(), deliverConfig.getType()));//配置回调
                    deliverOrder.setContactPhone(orderDetailResult.getContactPhone());
                    deliverOrder.setCouponAmount(orderDetailResult.getCouponAmount());

                    ZoneId zoneId = ZoneId.systemDefault();
                    LocalDateTime current = LocalDateTime.now();
                    ZonedDateTime zdt = current.atZone(zoneId);
                    Date dateStart = Date.from(zdt.toInstant());

                    deliverOrder.setCreateAt(dateStart);

                    current.plusHours(1);//加一个小时
                    Date dateEnd = Date.from(zdt.toInstant());
                    deliverOrder.setDeliverTime(DeliverTime.of("立即配送", dateStart, dateEnd));
                    deliverOrder.setDeliveryFee(orderDetailResult.getDeliveryAmount());
                    deliverOrder.setHdOrderCode(orderDetailResult.getHdOrderCode());
                    ResponseEntity<Store> storeResponseEntity = baseDataServiceFeign.findStoreByCode(orderDetailResult.getOrderStore().getStoreCode());
                    if (Objects.nonNull(storeResponseEntity) && storeResponseEntity.getStatusCode().is2xxSuccessful()) {
                        deliverOrder.setLat(storeResponseEntity.getBody().getLatitude().doubleValue());//TODO 配送经纬度不是门店的经纬度，是收货地址的经纬度
                        deliverOrder.setLng(storeResponseEntity.getBody().getLongitude().doubleValue());
                    } else {
                        log.error("查询门店信息失败", orderDetailResult.getOrderStore().getStoreCode());
                        deliverOrder.setLat(0.00);
                        deliverOrder.setLng(0.00);
                    }
                    deliverOrder.setOrderId(orderDetailResult.getId());
                    deliverOrder.setOrderCode(orderDetailResult.getCode());
                    deliverOrder.setReceiveUser(orderDetailResult.getReceiveUser());
                    deliverOrder.setRemark(orderDetailResult.getRemark());
                    deliverOrder.setStoreCode(orderDetailResult.getOrderStore().getStoreCode());
                    deliverOrder.setStoreName(orderDetailResult.getOrderStore().getStoreName());
                    deliverOrder.setTotalAmount(orderDetailResult.getTotalAmount());
                    deliverOrder.setUserId(orderDetailResult.getUserId());

                    List<DeliverProduct> deliverProductList = new ArrayList<>(orderDetailResult.getOrderProductList().size());
                    orderDetailResult.getOrderProductList().forEach(item -> {
                        DeliverProduct deliverProduct = new DeliverProduct();
                        deliverProduct.setBarcode(item.getBarcode());
                        deliverProduct.setBaseWeight(item.getTotalWeight().doubleValue());
                        deliverProduct.setDeliverBaseOrderId(orderDetailResult.getId());
                        deliverProduct.setDiscountPrice(item.getDiscountPrice());
                        deliverProduct.setImage(item.getImage());
                        deliverProduct.setLargeImage(item.getImage());
                        deliverProduct.setPrice(item.getTotalPrice());
                        deliverProduct.setProductName(item.getProductName());
                        deliverProduct.setProductQty(item.getProductQty());
                        deliverProduct.setSmallImage(item.getImage());
                        deliverProduct.setStandardPrice((int) Calculator.div(item.getTotalPrice(), item.getProductQty()));
                        deliverProduct.setStandardQty(Double.valueOf(item.getShelfQty().toString()));
                        deliverProductList.add(deliverProduct);
                    });
                    deliverOrder.setDeliverOrderProductList(deliverProductList);//填充订单商品

                    //发送达达配送
                    ResponseEntity<Tips> deliverResponseEntity = deliverServiceFeign.create(DeliverType.valueOf(deliverConfig.getType()), CoordinateSystem.AMAP, deliverOrder);

                    if (Objects.nonNull(deliverResponseEntity) && deliverResponseEntity.getStatusCode().is2xxSuccessful()) {
                        //设置成已发货
                        ResponseEntity sendOutResponse = orderServiceFeign.updateOrderStatus(orderDetailResult.getCode(), OrderStatus.SEND_OUT);
                        if (Objects.nonNull(sendOutResponse) && sendOutResponse.getStatusCode().is2xxSuccessful()) {
                            log.info("调用基础服务修改为已发货状态正常{}", orderCode);
                        } else {
                            log.error("调用基础服务修改为已发货状态错误{}", orderCode);
                        }
                    }
                }
                return Tips.of(HttpStatus.OK, orderCode);
            } else if ("return.received".equals(map.get("topic"))) {
                log.info("订单退货回调*********");
                if (Objects.equals(OrderStatus.RETURNING, orderDetailResult.getStatus())) {
                    log.info("给用户退款", orderDetailResult);
                    //退款
                    //判断退款是否成功
                    //改订单为退款成功
                }
            } else {
                log.info("hd other group message= " + map.get("group"));
            }
        }
        return Tips.of(HttpStatus.BAD_REQUEST, String.valueOf(orderDetailResultResponseEntity.getBody()));
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
                    return Tips.of(HttpStatus.OK, "配送待接单");
                case 2:
                    //调用基础服务修改为配送中状态
                    orderServiceFeign.updateOrderStatus(orderCode, OrderStatus.DISPATCHING);
                    deliverServiceFeign.update(orderCode, new DeliverUpdate(orderCode, DeliverStatus.WAIT_GET, stringParams.get("dm_name"), stringParams.get("dm_mobile"), null));
                    return Tips.of(HttpStatus.OK, "配送待取货");
                case 3:
                    deliverServiceFeign.update(orderCode, new DeliverUpdate(orderCode, DeliverStatus.DELIVERING, null, null, null));
                    return Tips.of(HttpStatus.OK, "配送配送中");
                case 4:
                    deliverServiceFeign.update(orderCode, new DeliverUpdate(orderCode, DeliverStatus.DONE, null, null, null));
                    orderServiceFeign.updateOrderStatus(orderCode, OrderStatus.RECEIVED);
                    return Tips.of(HttpStatus.OK, "配送配送完成");
                case 5:
                    deliverServiceFeign.update(orderCode, new DeliverUpdate(orderCode, DeliverStatus.FAILURE, null, null, "已取消"));
                    return Tips.of(HttpStatus.OK, "配送已取消");
                case 7:
                    deliverServiceFeign.update(orderCode, new DeliverUpdate(orderCode, DeliverStatus.FAILURE, null, null, "已过期"));
                    return Tips.of(HttpStatus.OK, "配送已过期");
                case 1000:
                    deliverServiceFeign.update(orderCode, new DeliverUpdate(orderCode, DeliverStatus.FAILURE, null, null, stringParams.get("cancel_reason")));
                    return Tips.of(HttpStatus.OK, "配送失败");
                default:
                    break;
            }
        } else {
            log.error("配送回调验证签名不通过");
        }
        //直接不作处理
        return Tips.of(HttpStatus.OK, "默认处理");
    }
}
