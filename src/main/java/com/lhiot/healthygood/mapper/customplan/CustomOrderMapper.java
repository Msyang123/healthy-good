package com.lhiot.healthygood.mapper.customplan;

import com.lhiot.healthygood.domain.customplan.CustomOrder;
import com.lhiot.healthygood.type.CustomOrderStatus;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Description:定制订单Mapper类
 *
 * @author yj
 * @date 2018/11/22
 */
@Mapper
@Repository
public interface CustomOrderMapper {

    /**
     * Description:新增定制订单
     *
     * @param customOrder
     * @return
     * @author yj
     * @date 2018/11/22 12:09:27
     */
    int create(CustomOrder customOrder);

    CustomOrder selectByCode(String code);

    int updateByCode(CustomOrder customOrder);

    List<CustomOrder> pageCustomOrder(CustomOrder customOrder);

    int pageCustomOrderCounts(CustomOrder customOrder);

    /**
     * 定制计划状态数目统计信息
     * @param userId
     * @return
     */
    Map statusCount(Long userId);

}
