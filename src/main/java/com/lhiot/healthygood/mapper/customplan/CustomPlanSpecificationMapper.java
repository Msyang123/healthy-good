package com.lhiot.healthygood.mapper.customplan;

import com.lhiot.healthygood.domain.customplan.CustomPlanSpecification;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
* Description:定制计划规格Mapper类
* @author hufan
* @date 2018/11/26
*/
@Mapper
@Repository
public interface CustomPlanSpecificationMapper {

    /**
    * Description:新增定制计划规格
    *
    * @param customPlanSpecification
    * @return
    * @author hufan
    * @date 2018/11/26 18:56:50
    */
    int create(CustomPlanSpecification customPlanSpecification);

    /**
    * Description:根据id修改定制计划规格
    *
    * @param customPlanSpecification
    * @return
    * @author hufan
    * @date 2018/11/26 18:56:50
    */
    int updateById(CustomPlanSpecification customPlanSpecification);

    /**
    * Description:根据ids删除定制计划规格
    *
    * @param ids
    * @return
    * @author hufan
    * @date 2018/11/26 18:56:50
    */
    int deleteByIds(List<String> ids);

    /**
    * Description:根据id查找定制计划规格
    *
    * @param id
    * @return
    * @author hufan
    * @date 2018/11/26 18:56:50
    */
    CustomPlanSpecification selectById(Long id);

    /**
    * Description:查询定制计划规格列表
    *
    * @param customPlanSpecification
    * @return
    * @author hufan
    * @date 2018/11/26 18:56:50
    */
     List<CustomPlanSpecification> pageCustomPlanSpecifications(CustomPlanSpecification customPlanSpecification);


    /**
    * Description: 查询定制计划规格总记录数
    *
    * @param customPlanSpecification
    * @return
    * @author hufan
    * @date 2018/11/26 18:56:50
    */
    long pageCustomPlanSpecificationCounts(CustomPlanSpecification customPlanSpecification);

    /**
     * 新增定制规格集合
     *
     * @param customPlanSpecifications 定制规格集合
     * @return 定制规格id
     */
    int insertList(List<CustomPlanSpecification> customPlanSpecifications);

}
