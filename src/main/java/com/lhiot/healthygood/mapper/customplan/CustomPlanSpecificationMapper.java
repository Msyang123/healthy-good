package com.lhiot.healthygood.mapper.customplan;

import com.lhiot.healthygood.domain.customplan.CustomPlanSpecification;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
* Description:定制计划规格Mapper类
* @author zhangs
* @date 2018/11/22
*/
@Mapper
public interface CustomPlanSpecificationMapper {

    /**
    * Description:新增定制计划规格
    *
    * @param customPlanSpecification
    * @return
    * @author zhangs
    * @date 2018/11/22 12:09:27
    */
    int create(CustomPlanSpecification customPlanSpecification);

    /**
    * Description:根据id修改定制计划规格
    *
    * @param customPlanSpecification
    * @return
    * @author zhangs
    * @date 2018/11/22 12:09:27
    */
    int updateById(CustomPlanSpecification customPlanSpecification);

    /**
    * Description:根据ids删除定制计划规格
    *
    * @param ids
    * @return
    * @author zhangs
    * @date 2018/11/22 12:09:27
    */
    int deleteByIds(List<String> ids);

    /**
    * Description:根据id查找定制计划规格
    *
    * @param id
    * @return
    * @author zhangs
    * @date 2018/11/22 12:09:27
    */
    CustomPlanSpecification selectById(Long id);

    /**
    * Description:查询定制计划规格列表
    *
    * @param customPlanSpecification
    * @return
    * @author zhangs
    * @date 2018/11/22 12:09:27
    */
     List<CustomPlanSpecification> pageCustomPlanSpecifications(CustomPlanSpecification customPlanSpecification);


    /**
    * Description: 查询定制计划规格总记录数
    *
    * @param customPlanSpecification
    * @return
    * @author zhangs
    * @date 2018/11/22 12:09:27
    */
    long pageCustomPlanSpecificationCounts(CustomPlanSpecification customPlanSpecification);

    List<CustomPlanSpecification> findByPlanIdAndPerid(Map<String,Object> param);
}
