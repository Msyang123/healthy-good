package com.lhiot.healthygood.mapper.customplan;

import com.lhiot.healthygood.domain.customplan.CustomPlanSection;
import com.lhiot.healthygood.domain.customplan.CustomPlanSectionParam;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* Description:定制计划板块Mapper类
* @author zhangs
* @date 2018/11/22
*/
@Mapper
public interface CustomPlanSectionMapper {

    /**
    * Description:新增定制计划板块
    *
    * @param customPlanSection
    * @return
    * @author zhangs
    * @date 2018/11/22 12:09:27
    */
    int create(CustomPlanSection customPlanSection);

    /**
    * Description:根据id修改定制计划板块
    *
    * @param customPlanSection
    * @return
    * @author zhangs
    * @date 2018/11/22 12:09:27
    */
    int updateById(CustomPlanSection customPlanSection);

    /**
    * Description:根据ids删除定制计划板块
    *
    * @param ids
    * @return
    * @author zhangs
    * @date 2018/11/22 12:09:27
    */
    int deleteByIds(List<String> ids);

    /**
    * Description:根据id查找定制计划板块
    *
    * @param id
    * @return
    * @author zhangs
    * @date 2018/11/22 12:09:27
    */
    CustomPlanSection selectById(Long id);

    List<CustomPlanSection> selectBySectionCode(String code);
    /**
    * Description:查询定制计划板块列表
    *
    * @param customPlanSectionParam
    * @return
    * @author zhangs
    * @date 2018/11/22 12:09:27
    */
     List<CustomPlanSection> pageCustomPlanSections(CustomPlanSectionParam customPlanSectionParam);


    /**
    * Description: 查询定制计划板块总记录数
    *
    * @param customPlanSectionParam
    * @return
    * @author zhangs
    * @date 2018/11/22 12:09:27
    */
    int pageCustomPlanSectionCounts(CustomPlanSectionParam customPlanSectionParam);
}
