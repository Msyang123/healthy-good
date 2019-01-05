package com.lhiot.healthygood.service.user;

import com.leon.microx.util.Beans;
import com.leon.microx.util.Maps;
import com.leon.microx.util.auditing.Random;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.lhiot.dc.dictionary.module.Dictionary;
import com.lhiot.healthygood.domain.doctor.DoctorAchievementLog;
import com.lhiot.healthygood.domain.doctor.RegisterApplication;
import com.lhiot.healthygood.domain.user.DoctorCustomer;
import com.lhiot.healthygood.domain.user.FruitDoctor;
import com.lhiot.healthygood.event.SendCaptchaSmsEvent;
import com.lhiot.healthygood.feign.BaseUserServerFeign;
import com.lhiot.healthygood.feign.model.OrderDetailResult;
import com.lhiot.healthygood.feign.model.UserDetailResult;
import com.lhiot.healthygood.feign.type.OrderStatus;
import com.lhiot.healthygood.mapper.user.DoctorCustomerMapper;
import com.lhiot.healthygood.mapper.user.FruitDoctorMapper;
import com.lhiot.healthygood.mq.HealthyGoodQueue;
import com.lhiot.healthygood.service.customplan.CustomPlanService;
import com.lhiot.healthygood.service.doctor.DoctorAchievementLogService;
import com.lhiot.healthygood.type.BalanceType;
import com.lhiot.healthygood.type.DoctorLevel;
import com.lhiot.healthygood.type.DoctorStatus;
import com.lhiot.healthygood.type.SourceType;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Description:鲜果师成员服务类
 *
 * @author yijun
 * @date 2018/07/26
 */
@Service
@Transactional
@Slf4j
public class FruitDoctorService {

    private final FruitDoctorMapper fruitDoctorMapper;
    private final DoctorCustomerMapper doctorCustomerMapper;
    private final ApplicationEventPublisher publisher;
    private final BaseUserServerFeign baseUserServerFeign;
    private final CustomPlanService customPlanService;
    private final DoctorAchievementLogService doctorAchievementLogService;
    private final RabbitTemplate rabbitTemplate;


    @Autowired
    public FruitDoctorService(FruitDoctorMapper fruitDoctorMapper, DoctorCustomerMapper doctorCustomerMapper, ApplicationEventPublisher publisher, BaseUserServerFeign baseUserServerFeign, CustomPlanService customPlanService, DoctorAchievementLogService doctorAchievementLogService, RedissonClient redissonClient, RabbitTemplate rabbitTemplate) {
        this.fruitDoctorMapper = fruitDoctorMapper;
        this.doctorCustomerMapper = doctorCustomerMapper;
        this.publisher = publisher;
        this.baseUserServerFeign = baseUserServerFeign;
        this.customPlanService = customPlanService;
        this.doctorAchievementLogService = doctorAchievementLogService;
        this.rabbitTemplate = rabbitTemplate;
        //初始化调用初始化accessToken 7100毫秒后调用
        HealthyGoodQueue.DelayQueue.REFRESH_ACCESS_TOKEN.send(this.rabbitTemplate, "nothing", 7100*1000);
    }

    /**
     * Description:新增鲜果师成员
     *
     * @param registerApplication
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public Tips create(RegisterApplication registerApplication) {
        // 幂等添加
        FruitDoctor doctor = fruitDoctorMapper.findFruitDoctorByUserId(registerApplication.getUserId());
        if (Objects.nonNull(doctor)) {
            return Tips.warn("该鲜果师已存在，添加失败");
        }
        FruitDoctor fruitDoctor = new FruitDoctor();
        //BeanUtils.copyProperties(registerApplication, fruitDoctor);
        Beans.from(registerApplication).populate(fruitDoctor);
        this.fruitDoctorMapper.create(fruitDoctor);
        fruitDoctor.setRealName(registerApplication.getRealName());
        fruitDoctor.setInviteCode(Random.of(4, Random.Digits._62));
        fruitDoctor.setDoctorLevel(DoctorLevel.TRAINING.toString());
        fruitDoctor.setDoctorStatus(DoctorStatus.VALID);
        fruitDoctor.setCreateAt(Date.from(Instant.now()));
        //查找推荐人
        DoctorCustomer doctorCustomer = doctorCustomerMapper.selectByUserId(registerApplication.getUserId());
        if (Objects.nonNull(doctorCustomer)) {
            fruitDoctor.setRefereeId(doctorCustomer.getDoctorId());
        }
        //查找基础服务对应的微信用户信息
        ResponseEntity<UserDetailResult> userEntity = baseUserServerFeign.findById(registerApplication.getUserId());
        if (userEntity.getStatusCode().isError()) {
            return Tips.warn(userEntity.getBody().toString());
        }
        UserDetailResult userDetailResult = userEntity.getBody();
        //设置头像默认为微信头像
        fruitDoctor.setAvatar(userDetailResult.getAvatar());
        fruitDoctor.setPhoto(userDetailResult.getAvatar());
        fruitDoctor.setUpperbodyPhoto(userDetailResult.getAvatar());
        fruitDoctor.setPhone(registerApplication.getPhone());
        fruitDoctor.setApplicationId(registerApplication.getId());
        fruitDoctor.setUserId(registerApplication.getUserId());
        return fruitDoctorMapper.create(fruitDoctor) > 0 ? Tips.info("添加鲜果师成员成功") : Tips.warn("添加鲜果师成员失败");
    }

    /**
     * Description:根据id修改鲜果师成员
     *
     * @param fruitDoctor
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public int updateById(FruitDoctor fruitDoctor) {

        // 数据修改

        return this.fruitDoctorMapper.updateById(fruitDoctor);
    }

    /**
     * Description:根据ids删除鲜果师成员
     *
     * @param ids
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public int deleteByIds(String ids) {
        return this.fruitDoctorMapper.deleteByIds(Arrays.asList(ids.split(",")));
    }

    /**
     * Description:根据id查找鲜果师成员
     *
     * @param id
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public FruitDoctor selectById(Long id) {
        return this.fruitDoctorMapper.selectById(id);
    }

    /**
     * Description:根据用户编号查找鲜果师成员
     *
     * @param userId
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public FruitDoctor selectByUserId(Long userId) {
        return this.fruitDoctorMapper.selectByUserId(userId);
    }

    /**
     * Description: 查询鲜果师成员总记录数
     *
     * @param fruitDoctor
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public int count(FruitDoctor fruitDoctor) {
        return this.fruitDoctorMapper.pageFruitDoctorCounts(fruitDoctor);
    }

    /**
     * Description: 查询鲜果师成员分页列表
     *
     * @param fruitDoctor
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public Pages<FruitDoctor> pageList(FruitDoctor fruitDoctor) {
        int total = 0;
        if (fruitDoctor.getRows() != null && fruitDoctor.getRows() > 0) {
            total = this.count(fruitDoctor);
        }
        return Pages.of(total,
                this.fruitDoctorMapper.pageFruitDoctors(fruitDoctor));
    }


    /**
     * Description: 查询鲜果师成员分页列表
     *
     * @param fruitDoctor
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public List<FruitDoctor> list(FruitDoctor fruitDoctor) {
        return this.fruitDoctorMapper.pageFruitDoctors(fruitDoctor);
    }

    /**
     * Description: 查询鲜果师团队分页列表
     *
     * @param fruitDoctor
     * @return
     * @author yangjiawen
     * @date 2018/07/26 12:08:13
     */
    public Pages<FruitDoctor> subordinate(FruitDoctor fruitDoctor) {
        int total = 0;
        if (fruitDoctor.getRows() != null && fruitDoctor.getRows() > 0) {
            total = this.count(fruitDoctor);
        }

        List<FruitDoctor> doctors =this.fruitDoctorMapper.subordinate(fruitDoctor).stream().filter(Objects::nonNull).map(item -> {
            Integer bouns = doctorAchievementLogService.superiorBonusOfMonth(Maps.of("doctorId",item.getId(),"currentMonth","yes"));
            item.setBounsOfMonth(item.getBounsOfMonth() + bouns);
            return item;
        }).collect(Collectors.toList());

        return Pages.of(total,
                doctors);
    }

    /**
     * 根据邀请码获取鲜果师
     *
     * @param inviteCode
     * @return
     */
    public FruitDoctor findDoctorByInviteCode(String inviteCode) {
        return fruitDoctorMapper.selectByInviteCode(inviteCode);
    }


    /**
     * Description: 根据用户id查询上级鲜果师信息
     *
     * @param userId
     * @return
     * @author hufan
     * @date 2018/08/21 12:08:13
     */
    public FruitDoctor findSuperiorFruitDoctorByUserId(Long userId) {
        return fruitDoctorMapper.findSuperiorFruitDoctorByUserId(userId);
    }


    /**
     * 鲜果师注册时绑定手机号码，发送模板消息
     *
     * @param phone 待发送验证码手机号
     */
    public void bandPhoneSendTemplateMessage(String phone) {
        //发送模板消息
        publisher.publishEvent(new SendCaptchaSmsEvent(phone));
        log.info("鲜果师注册时绑定手机号码，发送模板消息");
    }



    /**
     * 计算销售提成
     *
     * @param orderDetailResult
     */
    public void calculationCommission(OrderDetailResult orderDetailResult) {
        try {
            FruitDoctor doctor = this.findSuperiorFruitDoctorByUserId(orderDetailResult.getUserId());//查询用户的上级鲜果师
            if (Objects.isNull(doctor)) {
                log.info(orderDetailResult.getUserId() + " , " + "该用户没有上级鲜果师");
                return;
            }
            Optional<Dictionary.Entry> salesCommissions = customPlanService.dictionaryOptional("commissions").get().entry("SALES_COMMISSIONS");//销售提成百分比
            String commission = Objects.isNull(salesCommissions.get().getName()) ? "0" : salesCommissions.get().getName();
            Optional<Dictionary.Entry> bonus = customPlanService.dictionaryOptional("commissions").get().entry("BONUS");//红利提成百分比
            String fruitDoctorCommission = Objects.isNull(bonus.get().getName()) ? "0" : bonus.get().getName();
            Integer baseAmount = orderDetailResult.getAmountPayable();
            BigDecimal commissionBD = new BigDecimal(commission);
            BigDecimal fruitDoctorCommissionBD = new BigDecimal(fruitDoctorCommission);
            //计算销售提成
            Integer saleCommission = commissionBD.multiply(new BigDecimal(baseAmount)).intValue();
            Integer doctorCommission = fruitDoctorCommissionBD.multiply(new BigDecimal(baseAmount)).intValue();
            DoctorAchievementLog doctorAchievementLog = new DoctorAchievementLog();
            doctorAchievementLog.setOrderId(orderDetailResult.getCode());
            doctorAchievementLog.setUserId(orderDetailResult.getUserId());
            doctorAchievementLog.setAmount(baseAmount);
            doctorAchievementLog.setDoctorId(doctor.getId());

            DoctorAchievementLog logParam = new DoctorAchievementLog();
            logParam.setDoctorId(doctor.getId());
            logParam.setOrderId(orderDetailResult.getCode());
            String str = "";
            Integer orderCommission = 0;
            if (Objects.equals(OrderStatus.WAIT_SEND_OUT, orderDetailResult.getStatus()) || Objects.equals(OrderStatus.DISPATCHING, orderDetailResult.getStatus())) {
                logParam.setSourceType(SourceType.ORDER);
                doctorAchievementLog.setSourceType(SourceType.ORDER);
                doctorAchievementLog.setCommission(saleCommission);
                doctorAchievementLog.setFruitDoctorCommission(doctorCommission);
                str = "订单分成";
                orderCommission = saleCommission;
            } else if (Objects.equals(OrderStatus.ALREADY_RETURN, orderDetailResult.getStatus())) {
                doctorAchievementLog.setSourceType(SourceType.REFUND);
                doctorAchievementLog.setCommission(-saleCommission);
                doctorAchievementLog.setFruitDoctorCommission(-doctorCommission);
                logParam.setSourceType(SourceType.REFUND);
                str = "订单退款";
                orderCommission = -saleCommission;
            }
            Integer counts = doctorAchievementLogService.doctorAchievementLogCounts(logParam);//幂等操作，是否存在记录
            if (counts > 0) {
                log.info(orderDetailResult.getUserId() + "," + str + "," + "已执行过");
                return;
            }
            if (Objects.equals(OrderStatus.WAIT_SEND_OUT, orderDetailResult.getStatus()) || Objects.equals(OrderStatus.DISPATCHING, orderDetailResult.getStatus())
            || Objects.equals(OrderStatus.ALREADY_RETURN, orderDetailResult.getStatus())){
                if (doctorAchievementLogService.create(doctorAchievementLog) < 0) {
                    log.error(str + "存入失败");
                }
                Tips tips = doctorAchievementLogService.updateBonus(doctor.getId(),orderCommission, BalanceType.BOUNS);//鲜果师红利余额计算
                log.info(tips.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

