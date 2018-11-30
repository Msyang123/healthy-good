package com.lhiot.healthygood.mapper.customplan;

import com.lhiot.healthygood.domain.customplan.CustomPlan;
import com.lhiot.healthygood.domain.customplan.model.PlanSectionsParam;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanParam;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
* Description:定制计划Mapper类
* @author zhangs
* @date 2018/11/22
*/
@Mapper
@Repository
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
     * Description:根据name查找定制计划
     *
     * @param name
     * @return
     * @author hufan
     * @date 2018/11/26 20:08:07
     */
    CustomPlan selectByName(String name);

    /**
    * Description:查询定制计划列表
    *
    * @param customPlanParam
    * @return
    * @author zhangs
    * @date 2018/11/22 12:09:27
    */
     List<CustomPlan> pageCustomPlans(CustomPlanParam customPlanParam);


    /**
    * Description: 查询定制计划总记录数
    *
    * @param customPlanParam
    * @return
    * @author zhangs
    * @date 2018/11/22 12:09:27
    */
    int pageCustomPlanCounts(CustomPlanParam customPlanParam);

    List<CustomPlan> findByCustomPlanSectionId(PlanSectionsParam planSectionsParam);
}
