package com.lhiot.healthygood.mapper.customplan;

import com.lhiot.healthygood.domain.customplan.CustomOrderPause;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Description:定制订单Mapper类
 *
 * @author yj
 * @date 2018/11/22
 */
@Mapper
@Repository
public interface CustomOrderPauseMapper {

    /**
     * Description:新增定制订单暂停记录
     *
     * @param customOrderPause
     * @return
     * @author yj
     * @date 2018/11/22 12:09:27
     */
    int create(CustomOrderPause customOrderPause);

    /**
     * 恢复暂停记录
     * @param customOrderPause
     * @return
     */
    int update(CustomOrderPause customOrderPause);

    int updateByCodeBatch(List<CustomOrderPause> customOrderPauseList);

    CustomOrderPause selectCustomOrderPause(CustomOrderPause customOrderPause);

    int checkIfCustomOrderPauseExist(String customOrderCode);

    Integer selectHadPauseDays(String customOrderCode);

    List<CustomOrderPause> selectAll();

}
