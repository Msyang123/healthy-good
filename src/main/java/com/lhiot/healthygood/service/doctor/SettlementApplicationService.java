package com.lhiot.healthygood.service.doctor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.leon.microx.util.Maps;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.lhiot.healthygood.domain.doctor.DoctorAchievementLog;
import com.lhiot.healthygood.domain.doctor.SettlementApplication;
import com.lhiot.healthygood.domain.user.FruitDoctor;
import com.lhiot.healthygood.mapper.doctor.DoctorAchievementLogMapper;
import com.lhiot.healthygood.mapper.doctor.SettlementApplicationMapper;
import com.lhiot.healthygood.mapper.user.FruitDoctorMapper;
import com.lhiot.healthygood.mq.HealthyGoodQueue;
import com.lhiot.healthygood.type.*;
import com.lhiot.healthygood.util.DataItem;
import com.lhiot.healthygood.util.DataObject;
import com.lhiot.healthygood.wechat.WeChatUtil;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Description:结算申请服务类
 *
 * @author yijun
 * @date 2018/07/26
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SettlementApplicationService {

    private final SettlementApplicationMapper settlementApplicationMapper;
    private final FruitDoctorMapper fruitDoctorMapper;
    private final RabbitTemplate rabbit;
    private final DoctorAchievementLogMapper doctorAchievementLogMapper;
    private final WeChatUtil weChatUtil;

    @Autowired
    public SettlementApplicationService(SettlementApplicationMapper settlementApplicationMapper, FruitDoctorMapper fruitDoctorMapper, RabbitTemplate rabbit, DoctorAchievementLogMapper doctorAchievementLogMapper, WeChatUtil weChatUtil) {
        this.settlementApplicationMapper = settlementApplicationMapper;
        this.fruitDoctorMapper = fruitDoctorMapper;
        this.rabbit = rabbit;
        this.doctorAchievementLogMapper = doctorAchievementLogMapper;
        this.weChatUtil = weChatUtil;
        HealthyGoodQueue.DelayQueue.SETTLEMENT_EXPIRED.send(rabbit,"nothing",1 * 60 * 1000);
    }

    /**
     * Description:新增结算申请
     *
     * @param settlementApplication
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public int create(SettlementApplication settlementApplication) {
        return this.settlementApplicationMapper.create(settlementApplication);
    }

    /**
     * 结算进行扣款
     * @param settlementApplication
     * @return
     */
    public int settlement(SettlementApplication settlementApplication){
        boolean flag = fruitDoctorMapper.updateBouns(Maps.of("id", settlementApplication.getDoctorId(), "money", -settlementApplication.getAmount(), "balanceType", BalanceType.SETTLEMENT.name()))>0;
        if (!flag) {
            return -1;
        }
        int result = settlementApplicationMapper.create(settlementApplication);
        if (result>0){
            this.settlementApplicationSendTemplate(settlementApplication);
        }
        return result;
    }

    /**
     * 根据id修改结算申请
     *
     * @param id
     * @param settlementApplication
     * @return
     */
    public Tips updateById(Long id, SettlementApplication settlementApplication) {
        settlementApplication.setId(id);
        settlementApplication.setDealAt(Date.from(Instant.now()));
        // 已结算只能结算一次，成功后不可修改
        if (Objects.equals(SettlementStatus.SUCCESS, settlementApplication.getSettlementStatus())) {
            FruitDoctor findFruitDoctor = fruitDoctorMapper.selectById(settlementApplication.getDoctorId());
            // 结算成功后写操作记录
            // 幂等操作
            DoctorAchievementLog findDoctorAchievementLog = doctorAchievementLogMapper.selectByOrderIdAndType(id, SourceType.SETTLEMENT);
            if (Objects.nonNull(findDoctorAchievementLog)) {
                return Tips.warn("该用户结算记录已存在");
            }
            DoctorAchievementLog doctorAchievementLog = new DoctorAchievementLog();
            doctorAchievementLog.setDoctorId(settlementApplication.getDoctorId());
            doctorAchievementLog.setUserId(findFruitDoctor.getUserId());
            doctorAchievementLog.setAmount(-settlementApplication.getAmount());
            doctorAchievementLog.setSourceType(SourceType.SETTLEMENT);
            doctorAchievementLog.setCreateAt(Date.from(Instant.now()));
            doctorAchievementLog.setOrderId(id.toString());
            boolean addDoctorAchievementLog = doctorAchievementLogMapper.create(doctorAchievementLog) > 0;
            if (!addDoctorAchievementLog) {
                return Tips.warn("写鲜果师业绩记录失败");
            }
        }
        // 状态为未处理或者是已过期时 直接修改
        boolean settlementUpdated = settlementApplicationMapper.updateById(settlementApplication) > 0;
        return settlementUpdated ? Tips.info("结算修改成功") : Tips.warn("结算修改失败");
    }

    /**
     * 根据id修改结算申请
     *
     * @param id
     * @return
     */
    public Tips refund(Long id) {
        SettlementApplication settlementApplication = settlementApplicationMapper.selectById(id);
        // 修改结算申请记录
        SettlementApplication updateSettlement = new SettlementApplication();
        updateSettlement.setId(id);
        updateSettlement.setDealAt(Date.from(Instant.now()));
        updateSettlement.setSettlementStatus(SettlementStatus.REFUND);
        boolean settlementUpdate = settlementApplicationMapper.updateById(updateSettlement) > 0;
        if (!settlementUpdate) {
            return Tips.warn("修改结算申请记录失败");
        }
        // 增加鲜果师可结算金额
        FruitDoctor findFruitDoctor = fruitDoctorMapper.selectById(settlementApplication.getDoctorId());
        boolean settlementUpdated = fruitDoctorMapper.updateBouns(Maps.of("id", findFruitDoctor.getId(), "money", settlementApplication.getAmount(), "balanceType","SETTLEMENT")) > 0;

        if (!settlementUpdated) {
            return Tips.warn("增加鲜果师可结算金额失败");
        }

        // 结算成功后写操作记录
        // 幂等操作
        DoctorAchievementLog findDoctorAchievementLog = doctorAchievementLogMapper.selectByOrderIdAndType(id, SourceType.SETTLEMENT_REFUND);
        if (Objects.nonNull(findDoctorAchievementLog)) {
            return Tips.warn("该结算记录退款已存在");
        }
        DoctorAchievementLog doctorAchievementLog = new DoctorAchievementLog();
        doctorAchievementLog.setDoctorId(settlementApplication.getDoctorId());
        doctorAchievementLog.setUserId(findFruitDoctor.getUserId());
        doctorAchievementLog.setAmount(settlementApplication.getAmount());
        doctorAchievementLog.setSourceType(SourceType.SETTLEMENT_REFUND);
        doctorAchievementLog.setCreateAt(Date.from(Instant.now()));
        doctorAchievementLog.setOrderId(id.toString());
        //查询用户的上级鲜果师
        FruitDoctor doctor = fruitDoctorMapper.findSuperiorFruitDoctorByUserId(findFruitDoctor.getUserId());
        if (Objects.nonNull(doctor.getId())) {
            doctorAchievementLog.setSuperiorDoctorId(doctor.getId());
        }
        boolean addDoctorAchievementLog = doctorAchievementLogMapper.create(doctorAchievementLog) > 0;
        if (!addDoctorAchievementLog) {
            return Tips.warn("写鲜果师业绩记录失败");
        }
        return Tips.info("薪资结算退款成功");
    }

    /**
     * Description:根据ids删除结算申请
     *
     * @param ids
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public int deleteByIds(String ids) {
        return this.settlementApplicationMapper.deleteByIds(Arrays.asList(ids.split(",")));
    }

    /**
     * Description:根据id查找结算申请
     *
     * @param id
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public SettlementApplication selectById(Long id) {
        return this.settlementApplicationMapper.selectById(id);
    }

    /**
     * Description: 查询结算申请总记录数
     *
     * @param settlementApplication
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public int count(SettlementApplication settlementApplication) {
        return this.settlementApplicationMapper.pageSettlementApplicationCounts(settlementApplication);
    }

    /**
     * Description: 查询结算申请分页列表
     *
     * @param settlementApplication
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public Pages<SettlementApplication> pageList(SettlementApplication settlementApplication) {
        int total = 0;
        if (settlementApplication.getRows() != null && settlementApplication.getRows() > 0) {
            total = this.count(settlementApplication);
        }
        return Pages.of(total, this.settlementApplicationMapper.pageSettlementApplications(settlementApplication));
    }

    /**
     * 提现申请发送模板消息
     *
     * @param settlementApplication
     * @throws JsonProcessingException
     * @throws AmqpException
     */
    public void settlementApplicationSendTemplate(SettlementApplication settlementApplication) throws AmqpException{
        Integer amount = (null == settlementApplication.getAmount() ? 0 : settlementApplication.getAmount());
        //获取鲜果师用户信息
        FruitDoctor fruitDoctor = fruitDoctorMapper.selectById(settlementApplication.getDoctorId());
        //提现金额
        String fee = new BigDecimal(amount).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).toString();

        String currentTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        DataItem dataItem = new DataItem();
        DataObject first = new DataObject();
        DataObject keyword1 = new DataObject();
        DataObject keyword2 = new DataObject();
        DataObject remark = new DataObject();

        first.setValue(FirstAndRemarkData.NOTICE_OF_PRESENTATION.getFirstData());
        keyword1.setValue(currentTime);
        keyword2.setValue(fee);
        remark.setValue(FirstAndRemarkData.NOTICE_OF_PRESENTATION.getRemarkData());

        dataItem.setFirst(first);
        dataItem.setKeyword1(keyword1);
        dataItem.setKeyword2(keyword2);
        dataItem.setRemark(remark);
        weChatUtil.sendMessageToWechat(TemplateMessageEnum.NOTICE_OF_PRESENTATION, settlementApplication.getOpenId(), dataItem);

    }

    /**
     * Description: 修改结算申请失效状态
     *
     * @param list
     * @return
     * @author Limiaojun
     * @date 2018/08/22 09:52:13
     */
    public long updateExpiredStatus(List<Long> list) {

        return settlementApplicationMapper.updateExpiredStatus(list);
    }
}

