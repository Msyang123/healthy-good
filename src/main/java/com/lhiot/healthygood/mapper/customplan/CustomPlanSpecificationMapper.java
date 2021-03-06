package com.lhiot.healthygood.mapper.customplan;

import com.lhiot.healthygood.domain.customplan.CustomPlanSpecification;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
* Description:定制计划规格Mapper类
* @author zhangs
* @date 2018/11/22
*/
@Mapper
@Repository
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
     * Description:根据id修改定制计划规格
     *
     * @param specificationList
     * @return
     * @author hufan
     * @date 2018/12/22 15:53:47
     */
    int updateBatch(List<CustomPlanSpecification> specificationList);

    /**
     * Description:根据规格id修改定制计划规格
     *
     * @param customPlanSpecification
     * @return
     * @author hufan
     * @date 2018/12/22 15:53:47
     */
    int updateByStandardId(CustomPlanSpecification customPlanSpecification);

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
     * Description:根据计划id删除定制计划规格
     *
     * @param planIds
     * @return
     * @author hufan
     * @date 2018/11/22 12:09:27
     */
    int deleteByPlanIds(List<String> planIds);

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

    /**
     * Description: 根据定制计划ids查询定制计划规格
     *
     * @param planIds
     * @return
     * @author hufan
     * @date 2018/12/20 18:49:27
     */
    List<CustomPlanSpecification> findByPlanIds(List<Long> planIds);

    /**
     * 查找指定定制计划里面定制规格的最低价格
     * @param planId
     * @return
     */
    Long findMinPriceByPlanId(Long planId);

    /**
     * 新增定制规格集合
     *
     * @param customPlanSpecifications 定制规格集合
     * @return 定制规格id
     */
    int insertList(List<CustomPlanSpecification> customPlanSpecifications);


    /**
     * Description:根据基础规格数据ids查询定制计划规格
     *
     * @param ids
     * @return
     * @author hufan
     * @date 2019/01/12 11:32:03
     */
    List<CustomPlanSpecification> selectByStandardsIds(List<String> ids);

}
