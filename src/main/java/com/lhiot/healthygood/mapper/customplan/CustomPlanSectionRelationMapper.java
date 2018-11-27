package com.lhiot.healthygood.mapper.customplan;

import com.lhiot.healthygood.domain.customplan.CustomPlanSectionRelation;
import com.lhiot.healthygood.domain.customplan.CustomPlanSpecification;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
* Description:定制计划板块关联定制计划Mapper类
* @author zhangs
* @date 2018/11/22
*/
@Mapper
public interface CustomPlanSectionRelationMapper {

    /**
    * Description:新增定制计划板块关联定制计划
    *
    * @param customPlanSectionRelation
    * @return
    * @author zhangs
    * @date 2018/11/22 12:09:27
    */
    int create(CustomPlanSectionRelation customPlanSectionRelation);

    /**
    * Description:根据id修改定制计划板块关联定制计划
    *
    * @param customPlanSectionRelation
    * @return
    * @author zhangs
    * @date 2018/11/22 12:09:27
    */
    int updateById(CustomPlanSectionRelation customPlanSectionRelation);

    /**
    * Description:根据ids删除定制计划板块关联定制计划
    *
    * @param ids
    * @return
    * @author zhangs
    * @date 2018/11/22 12:09:27
    */
    int deleteByIds(List<String> ids);

    /**
    * Description:根据id查找定制计划板块关联定制计划
    *
    * @param id
    * @return
    * @author zhangs
    * @date 2018/11/22 12:09:27
    */
    CustomPlanSectionRelation selectById(Long id);

    /**
    * Description:查询定制计划板块关联定制计划列表
    *
    * @param customPlanSectionRelation
    * @return
    * @author zhangs
    * @date 2018/11/22 12:09:27
    */
     List<CustomPlanSectionRelation> pageCustomPlanSectionRelations(CustomPlanSectionRelation customPlanSectionRelation);


    /**
    * Description: 查询定制计划板块关联定制计划总记录数
    *
    * @param customPlanSectionRelation
    * @return
    * @author zhangs
    * @date 2018/11/22 12:09:27
    */
    long pageCustomPlanSectionRelationCounts(CustomPlanSectionRelation customPlanSectionRelation);

    List<CustomPlanSectionRelation> findByPlanId(Long planId);
}
