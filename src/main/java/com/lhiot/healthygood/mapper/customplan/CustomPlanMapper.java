package com.lhiot.healthygood.mapper.customplan;

import com.lhiot.healthygood.domain.customplan.CustomPlan;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* Description:定制计划Mapper类
* @author zhangs
* @date 2018/11/22
*/
@Mapper
public interface CustomPlanMapper {

    /**
    * Description:新增定制计划
    *
    * @param customPlan
    * @return
    * @author zhangs
    * @date 2018/11/22 12:09:27
    */
    int create(CustomPlan customPlan);

    /**
    * Description:根据id修改定制计划
    *
    * @param customPlan
    * @return
    * @author zhangs
    * @date 2018/11/22 12:09:27
    */
    int updateById(CustomPlan customPlan);

    /**
    * Description:根据ids删除定制计划
    *
    * @param ids
    * @return
    * @author zhangs
    * @date 2018/11/22 12:09:27
    */
    int deleteByIds(List<String> ids);

    /**
    * Description:根据id查找定制计划
    *
    * @param id
    * @return
    * @author zhangs
    * @date 2018/11/22 12:09:27
    */
    CustomPlan selectById(Long id);

    /**
    * Description:查询定制计划列表
    *
    * @param customPlan
    * @return
    * @author zhangs
    * @date 2018/11/22 12:09:27
    */
     List<CustomPlan> pageCustomPlans(CustomPlan customPlan);


    /**
    * Description: 查询定制计划总记录数
    *
    * @param customPlan
    * @return
    * @author zhangs
    * @date 2018/11/22 12:09:27
    */
    long pageCustomPlanCounts(CustomPlan customPlan);

    List<CustomPlan> findByCustomPlanSectionId(Long id);
}
