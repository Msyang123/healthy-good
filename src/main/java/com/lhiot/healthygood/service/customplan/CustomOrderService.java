package com.lhiot.healthygood.service.customplan;

import com.leon.microx.web.result.Tips;
import com.lhiot.healthygood.domain.customplan.CustomOrder;
import com.lhiot.healthygood.domain.customplan.CustomPlanSectionRelation;
import com.lhiot.healthygood.domain.customplan.CustomPlanSpecification;
import com.lhiot.healthygood.mapper.customplan.CustomOrderMapper;
import com.lhiot.healthygood.mapper.customplan.CustomPlanMapper;
import com.lhiot.healthygood.mapper.customplan.CustomPlanSpecificationMapper;
import com.lhiot.healthygood.type.CustomOrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Description:定制计划板块关联定制计划服务类
 *
 * @author hufan
 * @date 2018/11/26
 */
@Service
@Transactional
@Slf4j
public class CustomOrderService {

    private final CustomOrderMapper customOrderMapper;
    private final CustomPlanSpecificationMapper customPlanSpecificationMapper;

    @Autowired
    public CustomOrderService(CustomOrderMapper customOrderMapper, CustomPlanSpecificationMapper customPlanSpecificationMapper) {
        this.customOrderMapper = customOrderMapper;
        this.customPlanSpecificationMapper = customPlanSpecificationMapper;
    }

    /**
     * 新增定制计划订单
     *
     * @param customOrder 定制计划与版块关系对象
     * @return 定制计划与版块关系Id
     */
    public Tips createCustomOrder(CustomOrder customOrder) {
        CustomPlanSpecification customPlanSpecification = customPlanSpecificationMapper.selectById(customOrder.getSpecificationId());//查找指定定制规格
        if(Objects.isNull(customPlanSpecification))
            return Tips.warn("未找到定制规格");
        customOrder.setStatus(CustomOrderStatus.WAIT_PAYMENT);
        customOrder.setCreateAt(Date.from(Instant.now()));
        customOrder.setPrice(customPlanSpecification.getPrice());
        customOrder.setRemainingQty(customPlanSpecification.getPlanPeriod());//剩余配送次数就是周期数
        customOrder.setQuantity(customPlanSpecification.getQuantity());
        customOrder.setTotalQty(customPlanSpecification.getPlanPeriod());//总配送次数
        int result = customOrderMapper.create(customOrder);

        return result>0?Tips.info("创建成功"):Tips.warn("创建失败");
    }
}

