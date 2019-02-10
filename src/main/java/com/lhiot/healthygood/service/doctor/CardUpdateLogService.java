package com.lhiot.healthygood.service.doctor;

import com.leon.microx.web.result.Pages;
import com.lhiot.healthygood.domain.doctor.CardUpdateLog;
import com.lhiot.healthygood.domain.user.FruitDoctor;
import com.lhiot.healthygood.mapper.doctor.CardUpdateLogMapper;
import com.lhiot.healthygood.mapper.user.FruitDoctorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Objects;

/**
 * Description:服务类
 *
 * @author yangjiawen
 * @date 2018/07/26
 */
@Service
@Transactional
public class CardUpdateLogService {

    private final CardUpdateLogMapper cardUpdateLogMapper;
    private final FruitDoctorMapper fruitDoctorMapper;

    @Autowired
    public CardUpdateLogService(CardUpdateLogMapper cardUpdateLogMapper, FruitDoctorMapper fruitDoctorMapper) {
        this.cardUpdateLogMapper = cardUpdateLogMapper;
        this.fruitDoctorMapper = fruitDoctorMapper;
    }

    /**
     * Description:新增
     *
     * @param cardUpdateLog
     * @return
     * @author yangjiawen
     * @date 2019/01/2 12:08:13
     */
    public boolean create(CardUpdateLog cardUpdateLog) {
        boolean flag = false;
        if (Objects.isNull(cardUpdateLog.getId())){
            flag = this.cardUpdateLogMapper.create(cardUpdateLog) > 0;
        }else {
            flag = this.cardUpdateLogMapper.updateById(cardUpdateLog)>0;
        }
        if (flag) {
            FruitDoctor fruitDoctorParam = new FruitDoctor();
            fruitDoctorParam.setCardUsername(cardUpdateLog.getCardUsername());
            fruitDoctorParam.setBankDeposit(cardUpdateLog.getBankDeposit());
            fruitDoctorParam.setCardNo(cardUpdateLog.getCardNo());
            fruitDoctorParam.setId(cardUpdateLog.getDoctorId());
            if (fruitDoctorMapper.updateById(fruitDoctorParam) <= 0) {
                return false;
            }
        }
        return flag;
    }

    /**
     * Description:根据id修改
     *
     * @param cardUpdateLog
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public int updateById(CardUpdateLog cardUpdateLog) {
        return this.cardUpdateLogMapper.updateById(cardUpdateLog);
    }

    /**
     * Description:根据ids删除
     *
     * @param ids
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public int deleteByIds(String ids) {
        return this.cardUpdateLogMapper.deleteByIds(Arrays.asList(ids.split(",")));
    }

    /**
     * Description:根据id查找
     *
     * @param id
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public CardUpdateLog selectById(Long id) {
        return this.cardUpdateLogMapper.selectById(id);
    }

    public CardUpdateLog selectByCard(CardUpdateLog cardUpdateLog) {
        return this.cardUpdateLogMapper.selectByCard(cardUpdateLog);
    }

    /**
     * Description: 查询总记录数
     *
     * @param cardUpdateLog
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public int count(CardUpdateLog cardUpdateLog) {
        return this.cardUpdateLogMapper.pageCardUpdateLogCounts(cardUpdateLog);
    }

    /**
     * Description: 查询分页列表
     *
     * @param cardUpdateLog
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public Pages<CardUpdateLog> pageList(CardUpdateLog cardUpdateLog) {
        int total = 0;
        if (cardUpdateLog.getRows() != null && cardUpdateLog.getRows() > 0) {
            total = this.count(cardUpdateLog);
        }
        return Pages.of(total,
                this.cardUpdateLogMapper.pageCardUpdateLogs(cardUpdateLog));
    }
}

