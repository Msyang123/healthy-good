package com.lhiot.healthygood.service.doctor;

import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.lhiot.healthygood.domain.doctor.SettlementApplication;
import com.lhiot.healthygood.domain.user.FruitDoctor;
import com.lhiot.healthygood.mapper.doctor.SettlementApplicationMapper;
import com.lhiot.healthygood.mapper.user.FruitDoctorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

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

    @Autowired
    public SettlementApplicationService(SettlementApplicationMapper settlementApplicationMapper, FruitDoctorMapper fruitDoctorMapper) {
        this.settlementApplicationMapper = settlementApplicationMapper;
        this.fruitDoctorMapper = fruitDoctorMapper;
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
}

