package com.lhiot.healthygood.util;

import com.google.common.collect.ImmutableMap;
import com.leon.microx.probe.collector.ProbeEventPublisher;
import com.leon.microx.util.Jackson;
import com.leon.microx.util.Maps;
import com.leon.microx.util.StringUtils;
import com.lhiot.dc.dictionary.module.Dictionary;
import com.lhiot.healthygood.config.HealthyGoodConfig;
import com.lhiot.healthygood.domain.doctor.DoctorAchievementLog;
import com.lhiot.healthygood.domain.doctor.RegisterApplication;
import com.lhiot.healthygood.domain.template.TemplateData;
import com.lhiot.healthygood.domain.template.TemplateParam;
import com.lhiot.healthygood.domain.user.KeywordValue;
import com.lhiot.healthygood.feign.BaseUserServerFeign;
import com.lhiot.healthygood.feign.OrderServiceFeign;
import com.lhiot.healthygood.feign.model.OrderDetailResult;
import com.lhiot.healthygood.feign.model.UserDetailResult;
import com.lhiot.healthygood.feign.type.OrderStatus;
import com.lhiot.healthygood.service.customplan.CustomPlanService;
import com.lhiot.healthygood.service.doctor.DoctorAchievementLogService;
import com.lhiot.healthygood.service.doctor.RegisterApplicationService;
import com.lhiot.healthygood.service.user.FruitDoctorUserService;
import com.lhiot.healthygood.type.AuditStatus;
import com.lhiot.healthygood.type.FirstAndRemarkData;
import com.lhiot.healthygood.type.TemplateMessageEnum;
import com.lhiot.healthygood.wechat.WeChatUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Service
@Slf4j
public class MqConsume {

    private final WeChatUtil weChatUtil;
    private final FruitDoctorUserService fruitDoctorUserService;
    private final OrderServiceFeign orderServiceFeign;
    private final BaseUserServerFeign baseUserServerFeign;
    private final CustomPlanService customPlanService;
    private final RegisterApplicationService registerApplicationService;
    private final DoctorAchievementLogService doctorAchievementLogService;
    private ProbeEventPublisher publisher;

    @Autowired
    public MqConsume(FruitDoctorUserService fruitDoctorUserService, HealthyGoodConfig properties,
                     RegisterApplicationService registerApplicationService, OrderServiceFeign orderServiceFeign, BaseUserServerFeign baseUserServerFeign, CustomPlanService customPlanService, RegisterApplicationService registerApplicationService1, DoctorAchievementLogService doctorAchievementLogService, ProbeEventPublisher publisher) {
        this.orderServiceFeign = orderServiceFeign;
        this.weChatUtil = new WeChatUtil(properties);
        this.fruitDoctorUserService = fruitDoctorUserService;
        this.baseUserServerFeign = baseUserServerFeign;
        this.customPlanService = customPlanService;
        this.registerApplicationService = registerApplicationService1;
        this.doctorAchievementLogService = doctorAchievementLogService;
        this.publisher = publisher;
    }

    //计算业绩提成
    @RabbitHandler
    @RabbitListener(queues = "fruit_doctor_calculation_bonus")
    public void fruitDoctorCalculationBonus(String baseOrderInfo) {
        try {
            log.info("=======================>队列baseOrderInfo:" + baseOrderInfo);
            //计算提成
            if (StringUtils.isBlank(baseOrderInfo)) {
                log.info("=======================>队列参数为空baseOrderInfo:" + baseOrderInfo);
                return;
            }
            DoctorAchievementLog doctorAchievementLog = new DoctorAchievementLog();
            OrderDetailResult order = Jackson.object(baseOrderInfo, OrderDetailResult.class);
            if (Objects.isNull(order)) {
                return;
            }
            Long orderId = order.getId();
            if (Objects.isNull(orderId) || Objects.equals(0L, orderId)) {
                return;
            }
            OrderStatus status = order.getStatus();
            //计算提成的基数
            Integer baseAmount = order.getAmountPayable();
            //退货--金额为负数
            if (Objects.equals(OrderStatus.RETURNING, status) || Objects.equals(OrderStatus.ALREADY_RETURN, status)) {
                doctorAchievementLog.setSourceType("REFUND");
                baseAmount = 0 - baseAmount;
            }
            String commission = "0";
            String fruitDoctorCommission = "0";
            //feign系统参数获取分成比例
            if (OrderStatus.WAIT_SEND_OUT.equals(status) || OrderStatus.DISPATCHING.equals(status)) {
                Optional<Dictionary.Entry> salesCommissions = customPlanService.dictionaryOptional("COMMISSION").get().entry("SALES_COMMISSIONS");
                if (Objects.nonNull(salesCommissions)) {
                    String value = salesCommissions.get().getName();
                    commission = Objects.isNull(value) ? "0" : value;
                    log.info("====================>提成比例:commission:" + commission);
                }
                //鲜果师分销提成比例
                Optional<Dictionary.Entry> bonus = customPlanService.dictionaryOptional("COMMISSION").get().entry("BONUS");
                if (Objects.nonNull(bonus)) {
                    String value = bonus.get().getName();
                    fruitDoctorCommission = Objects.isNull(value) ? "0" : value;
                    log.info("====================>上级鲜果师提成比例:fruitDoctorCommission:" + fruitDoctorCommission);
                }
            } else {
                DoctorAchievementLog dl = doctorAchievementLogService.findByOrderId(orderId);
                if (Objects.isNull(dl)) {
                    log.info("====================>退货时获取退款比例失败:DoctorBonusLog:" + dl);
                    return;
                }
                commission = dl.getCommission().toString();
                fruitDoctorCommission = dl.getFruitDoctorCommission().toString();
            }

            BigDecimal commissionBD = new BigDecimal(commission);
            BigDecimal fruitDoctorCommissionBD = new BigDecimal(fruitDoctorCommission);
            //计算销售提成
            Integer saleCommission = commissionBD.multiply(new BigDecimal(baseAmount)).intValue();
            //计算分销提成
            Integer fruitCommission = fruitDoctorCommissionBD.multiply(new BigDecimal(baseAmount)).intValue();

            Map<String, Object> map = ImmutableMap.of("saleCommission", saleCommission, "fruitCommission", fruitCommission);

            doctorAchievementLog.setCommission(commissionBD.doubleValue());
            doctorAchievementLog.setFruitDoctorCommission(fruitDoctorCommissionBD.doubleValue());
            doctorAchievementLog.setOrderId(order.getId());
            doctorAchievementLog.setUserId(order.getUserId());
            doctorAchievementLog.setCommissionMap(map);
            //写入提成
            doctorAchievementLogService.create(doctorAchievementLog);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            publisher.mqConsumerException(e, Maps.of("message", "计算业绩提成消息队列出错-fruit_doctor_calculation_bonus"));
        }
    }


    @RabbitHandler
    @RabbitListener(queues = "fruit_doctor_send-tmpessage")
    public void sendTemplate(String keyWordValueMessage) {
        try {
            log.info("模板消息推送入参：" + keyWordValueMessage);
            KeywordValue keywordValue = Jackson.object(keyWordValueMessage, KeywordValue.class);
            if (Objects.isNull(keywordValue)) {
                log.info("=====================>入参数为空，keywordValue:" + keywordValue);
                return;
            }
            boolean sendToDoctor = keywordValue.isSendToDoctor();
            Long userId = keywordValue.getUserId();
            String keyword1 = "keyword1";
            String keyword2 = "keyword2";
            //获取客户信息
            ResponseEntity<UserDetailResult> userDetailResultResponseEntity = baseUserServerFeign.findById(userId);
            if (userDetailResultResponseEntity.getStatusCode().is2xxSuccessful() && Objects.isNull(userDetailResultResponseEntity.getBody())) {
                log.info("=====================>获取用户数据失败，userId:" + userId);
                return;
            }
            UserDetailResult user = userDetailResultResponseEntity.getBody();

            //获取订单信息
            String orderCode = keywordValue.getOrderCode();
            OrderDetailResult order = null;
            if (StringUtils.isNotBlank(orderCode)) {
                ResponseEntity<OrderDetailResult> orderEntity = orderServiceFeign.orderDetail(orderCode, false, false);
                if (Objects.nonNull(orderEntity) && orderEntity.getStatusCodeValue() < 400) {
                    order = orderEntity.getBody();
                }
            }
            //构建发送模板消息的参数
            TemplateParam userTParam = new TemplateParam();
            userTParam.setTouser(user.getOpenId());

            String keyword3Value = keywordValue.getKeyword3Value();
            String url = "";
            //鲜果师参数
            TemplateParam doctorTParam = null;
            switch (keywordValue.getTemplateType()) {
                //会员加入提醒
                case MEMBER_SHIP:
                    url = TemplateMessageEnum.MEMBER_SHIP.getUrl();
                    userTParam.setTemplateId(TemplateMessageEnum.MEMBER_SHIP.getTemplateId());
                    //给鲜果师发送消息的参数
                    doctorTParam = this.doctorTemplateParam(sendToDoctor, TemplateMessageEnum.MEMBER_SHIP.getTemplateId(),
                            TemplateMessageEnum.MEMBER_SHIP.getUrl(), userId);
                    break;
                //购买成功通知
                case PURCHASE_NOTICE:
                    if (Objects.nonNull(order)) {
                        url = TemplateMessageEnum.PURCHASE_NOTICE.getUrl() + order.getId();
                    }
                    userTParam.setTemplateId(TemplateMessageEnum.PURCHASE_NOTICE.getTemplateId());
                    keyword3Value = new BigDecimal(keyword3Value).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP) + "元";
                    //给鲜果师发送消息的参数
                    doctorTParam = this.doctorTemplateParam(sendToDoctor, TemplateMessageEnum.PURCHASE_NOTICE.getTemplateId(),
                            TemplateMessageEnum.PURCHASE_NOTICE.getUrl(), userId);
                    break;
                //申请鲜果师
                case APPLY_FRUIT_DOCTOR:
                    url = TemplateMessageEnum.APPLY_FRUIT_DOCTOR.getUrl();
                    userTParam.setTemplateId(TemplateMessageEnum.APPLY_FRUIT_DOCTOR.getTemplateId());
                    //给鲜果师发送消息的参数
                    doctorTParam = this.doctorTemplateParam(sendToDoctor, TemplateMessageEnum.APPLY_FRUIT_DOCTOR.getTemplateId(),
                            TemplateMessageEnum.APPLY_FRUIT_DOCTOR.getUrl(), userId);
                    break;
                //订单状态变更通知
                case ORDER_REMINDING:
                    keyword1 = "OrderSn";
                    keyword2 = "OrderStatus";
                    if (Objects.nonNull(order)) {
                        url = TemplateMessageEnum.ORDER_REMINDING.getUrl() + order.getId();
                    }
                    userTParam.setTemplateId(TemplateMessageEnum.ORDER_REMINDING.getTemplateId());
                    //给鲜果师发送消息的参数
                    doctorTParam = this.doctorTemplateParam(sendToDoctor, TemplateMessageEnum.ORDER_REMINDING.getTemplateId(),
                            TemplateMessageEnum.ORDER_REMINDING.getUrl(), userId);
                    break;
                //提现申请通知
                case NOTICE_OF_PRESENTATION:
                    url = TemplateMessageEnum.NOTICE_OF_PRESENTATION.getUrl();
                    userTParam.setTemplateId(TemplateMessageEnum.NOTICE_OF_PRESENTATION.getTemplateId());
                    //给鲜果师发送消息的参数
                    doctorTParam = this.doctorTemplateParam(sendToDoctor, TemplateMessageEnum.NOTICE_OF_PRESENTATION.getTemplateId(),
                            TemplateMessageEnum.NOTICE_OF_PRESENTATION.getUrl(), userId);
                    this.doctorTemplateParam(sendToDoctor, TemplateMessageEnum.NOTICE_OF_PRESENTATION.getTemplateId(),
                            TemplateMessageEnum.NOTICE_OF_PRESENTATION.getUrl(), userId);
                    break;
                //推荐上明星鲜果师通知
                case UPGRADE_FRUIT_DOCTOR:
                    url = TemplateMessageEnum.UPGRADE_FRUIT_DOCTOR.getUrl();
                    userTParam.setTemplateId(TemplateMessageEnum.UPGRADE_FRUIT_DOCTOR.getTemplateId());
                    break;
                default:
                    break;
            }
            if (Objects.isNull(userTParam.getTemplateId())) {
                log.info("=====================>templateId为");
                return;
            }
            //获取firstData和remarkData
            TemplateData td = this.firstAndRemarkData(keywordValue, order);
            if (Objects.isNull(td)) {
                log.info("=====================>获取订单信息失败");
                return;
            }
            //设置跳转的url
            if (StringUtils.isNotBlank(url)) {
                userTParam.setUrl(url);
            }
            Map<String, Object> member = td.getMember();
            Map<String, Object> doctor = td.getDoctor();

            Map<String, Object> data = new HashMap<>();
            data.put(keyword1, ImmutableMap.of("value", keywordValue.getKeyword1Value()));
            data.put(keyword2, ImmutableMap.of("value", keywordValue.getKeyword2Value()));
            //Fixme 这里有问题
            /*data.put("keyword3", ImmutableMap.of("value", keyword3Value));
            data.put("keyword4", ImmutableMap.of("value", keywordValue.getKeyword4Value()));
            data.put("keyword5", ImmutableMap.of("value", keywordValue.getKeyword5Value()));*/
            //鲜果师参数
            Map<String, Object> doctorData = new HashMap<>();
            doctorData.putAll(data);
            //设置firstData和remarkData
            data.putAll(member);
            //userTParam.setData(data);
            log.info("=======================>模板消息推送参数" + Jackson.json(userTParam));
            String result = weChatUtil.sendTemplateMessage(Jackson.json(userTParam));
            log.info("=======================>模板消息推送给会员返回结果" + result);
            //判断是否需要给鲜果师，发送模板消息
            if (Objects.nonNull(doctorTParam)) {
                doctorData.putAll(doctor);
                //doctorTParam.setData(doctorData);
                String doctorResult = weChatUtil.sendTemplateMessage(Jackson.json(doctorTParam));
                log.info("=======================>模板消息推送给鲜果师返回结果" + doctorResult);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            publisher.mqConsumerException(e, Maps.of("message", "鲜果师提现发送模板消息队列出错-fruit_doctor_send-tmpessage"));
        }
    }

    /**
     * 给鲜果师发送模板消息
     *
     * @param isSendToDotor 是否发送鲜果师 true-需要，false-不需要
     * @param templateId    模板id
     * @param url           跳转的url
     * @param userId        鲜果师会员id
     */
    public TemplateParam doctorTemplateParam(boolean isSendToDotor, String templateId, String url, Long userId) {
        if (!isSendToDotor) {
            return null;
        }
        ResponseEntity<UserDetailResult> userDetailResultResponseEntity = baseUserServerFeign.findById(userId);
        if (userDetailResultResponseEntity.getStatusCode().is2xxSuccessful() && Objects.isNull(userDetailResultResponseEntity.getBody())) {
            log.info("=====================>获取用户数据失败，userId:" + userId);
            return new TemplateParam();
        }
        UserDetailResult user = userDetailResultResponseEntity.getBody();
        if (Objects.isNull(user)) {
            return null;
        }
        //构建发送模板消息的数据
        TemplateParam template = new TemplateParam();
        template.setTemplateId(templateId);
        template.setUrl(url);
        template.setTouser(user.getOpenId());

        return template;
    }



    /**
     * 发送模板消息的firstdata和remarkdata
     *
     * @param keywordValue
     * @return
     */
    public TemplateData firstAndRemarkData(KeywordValue keywordValue, OrderDetailResult order) {
        Long userId = keywordValue.getUserId();
        String fistData = "";
        String remarkData = "";
        Map<String, Object> doctor = null;
        boolean sendToDoctor = keywordValue.isSendToDoctor();

        switch (keywordValue.getTemplateType()) {
            case MEMBER_SHIP: //会员加入提醒
                fistData = FirstAndRemarkData.MEMBER_SHIP_USER.getFirstData();
                remarkData = FirstAndRemarkData.MEMBER_SHIP_USER.getRemarkData();
                //是否发送给鲜果师
                if (sendToDoctor) {
                    doctor = ImmutableMap.of("first", ImmutableMap.of("value", FirstAndRemarkData.MEMBER_SHIP_DOCTOR.getFirstData()),
                            "remark", ImmutableMap.of("value", FirstAndRemarkData.MEMBER_SHIP_DOCTOR.getRemarkData()));
                }
                break;
            case PURCHASE_NOTICE: //购买成功通知
                fistData = FirstAndRemarkData.PURCHASE_NOTICE_USER.getFirstData();
                remarkData = FirstAndRemarkData.PURCHASE_NOTICE_USER.getRemarkData();

                BigDecimal commissionBig = new BigDecimal(0);
                Optional<Dictionary.Entry> salesCommissions = customPlanService.dictionaryOptional("commissions").get().entry("SALES_COMMISSIONS");
                if (Objects.nonNull(salesCommissions)) {
                    String comission = Objects.isNull(salesCommissions.get().getName()) ? "0" : salesCommissions.get().getName();
                    commissionBig = new BigDecimal(comission);
                }
                //是否发送给鲜果师
                if (sendToDoctor) {
                    if (Objects.isNull(order)) {
                        return null;
                    }
                    String doctorFistData = FirstAndRemarkData.PURCHASE_NOTICE_DOCTOR.getFirstData();
                    Integer amountPayable = Objects.isNull(order.getAmountPayable()) ? 0 : order.getAmountPayable();
                    String income = new BigDecimal(amountPayable).multiply(commissionBig)
                            .divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).toString();
                    doctorFistData = doctorFistData.replace("@BALANCE@", income);
                    doctor = ImmutableMap.of("first", ImmutableMap.of("value", doctorFistData),
                            "remark", ImmutableMap.of("value", FirstAndRemarkData.PURCHASE_NOTICE_DOCTOR.getRemarkData()));
                }
                break;
            case APPLY_FRUIT_DOCTOR: //申请鲜果师
                RegisterApplication registerApplication = registerApplicationService.findLastApplicationById(userId);
                AuditStatus status = registerApplication.getAuditStatus();
                if ("AGREE".equals(status)) {
                    fistData = FirstAndRemarkData.APPLY_SUCCESS.getFirstData();
                    remarkData = FirstAndRemarkData.APPLY_SUCCESS.getRemarkData();
                } else if ("REJECT".equals(status)) {
                    fistData = FirstAndRemarkData.APPLY_FAILURE.getFirstData();
                    remarkData = FirstAndRemarkData.APPLY_FAILURE.getRemarkData();
                } else {
                    fistData = FirstAndRemarkData.APPLY.getFirstData();
                    remarkData = FirstAndRemarkData.APPLY.getRemarkData();
                }
                break;
            case ORDER_REMINDING: //订单状态变更通知
                if (Objects.isNull(order)) {
                    return null;
                }
                OrderStatus orderStatus = order.getStatus();
                if (OrderStatus.DISPATCHING.equals(orderStatus)) {
                    fistData = FirstAndRemarkData.ORDER_DISPATCHING.getFirstData();
                    remarkData = FirstAndRemarkData.ORDER_DISPATCHING.getRemarkData();
                } else if (OrderStatus.RETURNING.equals(orderStatus)) {
                    fistData = FirstAndRemarkData.ORDER_RETURNNING.getFirstData();
                    remarkData = FirstAndRemarkData.ORDER_RETURNNING.getRemarkData();
                } else if (OrderStatus.ALREADY_RETURN.equals(orderStatus)) {
                    fistData = FirstAndRemarkData.ORDER_ALREADY_RETURN.getFirstData();
                    remarkData = FirstAndRemarkData.ORDER_ALREADY_RETURN.getRemarkData();
                }
                break;
            case NOTICE_OF_PRESENTATION: //提现申请通知
                fistData = FirstAndRemarkData.NOTICE_OF_PRESENTATION.getFirstData();
                remarkData = FirstAndRemarkData.NOTICE_OF_PRESENTATION.getRemarkData();
                break;
            case UPGRADE_FRUIT_DOCTOR: //推荐上明星鲜果师通知
                fistData = FirstAndRemarkData.UPGRADE_FRUIT_DOCTOR.getFirstData();
                remarkData = FirstAndRemarkData.UPGRADE_FRUIT_DOCTOR.getRemarkData();
                break;
            default:
                break;
        }
        TemplateData td = new TemplateData();
        td.setMember(ImmutableMap.of("first", ImmutableMap.of("value", fistData), "remark", ImmutableMap.of("value", remarkData)));
        if (Objects.nonNull(doctor)) {
            td.setDoctor(doctor);
        }
        return td;
    }
}
