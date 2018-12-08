package com.lhiot.healthygood.mapper.customplan;

import com.lhiot.healthygood.domain.customplan.CustomPlanSpecificationStandard;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanSpecificationStandardParam;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Description:定制计划规格基础数据Mapper类
 *
 * @author hufan
 * @date 2018/12/08
 */
@Mapper
@Repository
public interface CustomPlanSpecificationStandardMapper {

    /**
     * Description:新增定制计划规格基础数据
     *
     * @param customPlanSpecificationStandard
     * @return
     * @author hufan
     * @date 2018/12/08 09:07:00
     */
    int create(CustomPlanSpecificationStandard customPlanSpecificationStandard);

    /**
     * Description:根据id修改定制计划规格基础数据
     *
     * @param customPlanSpecificationStandard
     * @return
     * @author hufan
     * @date 2018/12/08 09:07:00
     */
    int updateById(CustomPlanSpecificationStandard customPlanSpecificationStandard);

    /**
     * Description:根据ids删除定制计划规格基础数据
     *
     * @param ids
     * @return
     * @author hufan
     * @date 2018/12/08 09:07:00
     */
    int deleteByIds(List<String> ids);

    /**
     * Description:根据id查找定制计划规格基础数据
     *
     * @param id
     * @return
     * @author hufan
     * @date 2018/12/08 09:07:00
     */
    CustomPlanSpecificationStandard selectById(Long id);

    /**
     * Description:查询定制计划规格基础数据列表
     *
     * @param customPlanSpecificationStandardParam
     * @return
     * @author hufan
     * @date 2018/12/08 09:07:00
     */
    List<CustomPlanSpecificationStandard> pageCustomPlanSpecificationStandards(CustomPlanSpecificationStandardParam customPlanSpecificationStandardParam);


    /**
     * Description: 查询定制计划规格基础数据总记录数
     *
     * @param customPlanSpecificationStandardParam
     * @return
     * @author hufan
     * @date 2018/12/08 09:07:00
     */
    int pageCustomPlanSpecificationStandardCounts(CustomPlanSpecificationStandardParam customPlanSpecificationStandardParam);

    /**
     * Description: 查询定制计划规格基础数据列表
     *
     * @param
     * @return
     * @author hufan
     * @date 2018/12/08 09:07:00
     */
    List<CustomPlanSpecificationStandard> findList();
}
