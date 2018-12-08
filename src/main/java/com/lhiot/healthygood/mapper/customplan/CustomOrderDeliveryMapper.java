package com.lhiot.healthygood.mapper.customplan;

import com.lhiot.healthygood.domain.customplan.CustomOrderDelivery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Description:个人购买定制计划领取记录Mapper类
 *
 * @author yj
 * @date 2018/11/22
 */
@Mapper
@Repository
public interface CustomOrderDeliveryMapper {

    /**
     * Description:新增个人购买定制计划领取记录
     *
     * @param customOrderDelivery
     * @return
     * @author yj
     * @date 2018/11/22 12:09:27
     */
    int create(CustomOrderDelivery customOrderDelivery);

    int updateByOrderCode(String orderCode);

    List<CustomOrderDelivery> selectByCustomOrderId(@Param("planId") Long planId,@Param("customOrderId") Long customOrderId);


}
