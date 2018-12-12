package com.lhiot.healthygood.service.doctor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.leon.microx.util.Jackson;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.lhiot.healthygood.domain.doctor.SettlementApplication;
import com.lhiot.healthygood.domain.user.FruitDoctor;
import com.lhiot.healthygood.domain.user.KeywordValue;
import com.lhiot.healthygood.mapper.doctor.SettlementApplicationMapper;
import com.lhiot.healthygood.mapper.user.FruitDoctorMapper;
import com.lhiot.healthygood.type.FruitDoctorOrderExchange;
import com.lhiot.healthygood.type.TemplateMessageEnum;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

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

    @Autowired
    public SettlementApplicationService(SettlementApplicationMapper settlementApplicationMapper, FruitDoctorMapper fruitDoctorMapper, RabbitTemplate rabbit) {
        this.settlementApplicationMapper = settlementApplicationMapper;
        this.fruitDoctorMapper = fruitDoctorMapper;
        this.rabbit = rabbit;
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
     * Description:根据id修改结算申请
     *
     * @param settlementApplication
     * @param fruitDoctor
     * @return
     * @author hfuan
     * @date 2018/12/07 12:08:13
     */
    public Tips updateById(SettlementApplication settlementApplication, FruitDoctor fruitDoctor) {
        boolean settlementUpdated = settlementApplicationMapper.updateById(settlementApplication) > 0;
        if (!settlementUpdated) {
            return Tips.warn("结算失败");
        }
        // 结算状态修改成功后扣减用户可结算金额
        fruitDoctor.setBalance(fruitDoctor.getBalance() - settlementApplication.getAmount());
        boolean balanceUpdated = fruitDoctorMapper.updateById(fruitDoctor) > 0;
        if (!balanceUpdated) {
            return Tips.warn("扣减鲜果师可结算金额失败");
        }
        return Tips.info("结算成功");
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
     * @param settlementApplication
     * @throws JsonProcessingException
     * @throws AmqpException
     */
    public void settlementApplicationSendToQueue(SettlementApplication settlementApplication) throws AmqpException, JsonProcessingException{
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        Integer amount = (null == settlementApplication.getAmount() ? 0 : settlementApplication.getAmount());
        //获取鲜果师用户信息
        FruitDoctor fruitDoctor = fruitDoctorMapper.selectById(settlementApplication.getDoctorId());
        //提现金额
        String fee = new BigDecimal(amount).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).toString();

        KeywordValue keywordValue = new KeywordValue();
        keywordValue.setTemplateType(TemplateMessageEnum.NOTICE_OF_PRESENTATION);
        keywordValue.setKeyword1Value(currentTime);
        keywordValue.setKeyword2Value(fee);

        keywordValue.setUserId(fruitDoctor.getUserId());
        keywordValue.setSendToDoctor(false);

        //发送模板消息
        rabbit.convertAndSend(FruitDoctorOrderExchange.FRUIT_TEMPLATE_MESSAGE.getExchangeName(),
                FruitDoctorOrderExchange.FRUIT_TEMPLATE_MESSAGE.getQueueName(), Jackson.json(keywordValue));
    }
}

