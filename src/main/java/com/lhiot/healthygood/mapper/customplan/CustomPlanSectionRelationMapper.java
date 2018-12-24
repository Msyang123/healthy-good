package com.lhiot.healthygood.mapper.customplan;

import com.lhiot.healthygood.domain.customplan.CustomPlanSectionRelation;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanSectionRelationResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
* Description:定制计划板块关联定制计划Mapper类
* @author zhangs
* @date 2018/11/22
*/
@Mapper
@Repository
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
     * 新增批量定制计划与版块关系
     *
     * @param list 定制计划与版块关系集合
     * @return 执行结果
     */
    int insertList(List<CustomPlanSectionRelation> list);

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

    /**
     * 根据定制计划ID集合 删除定制计划与版块关系记录
     *
     * @param shelfIds 定制计划ID集合
     * @return 执行结果
     */
    int deleteRelationByShelfIds(@Param("shelfIds") String shelfIds);


    /**
     * 根据定制版块ID集合 删除定制计划与版块关系记录
     *
     * @param sectionIds 定制版块ID集合
     * @return 执行结果
     */
    int deleteRelationBySectionIds(@Param("sectionIds") String sectionIds);


    /**
     * 批量删除定制计划与版块关系记录
     *
     * @param sectionId 定制版块ID
     * @param planIds  定制计划ID集合
     * @return 执行结果
     */
    int deleteRelationList(@Param("sectionId") Long sectionId, @Param("planIds") String planIds);

    /**
     * 批量删除定制计划与版块关系记录
     *
     * @param planIds  定制计划ID集合
     * @return 执行结果
     */
    int deleteByPlanIds(List<String> planIds);

    /**
     * 根据定制板块id 查询关联的定制计划信息
     * @param sectionId
     * @return
     */
    List<CustomPlanSectionRelationResult> findPlanBySectionId(@Param("sectionId") Long sectionId);

    /**
     * 根据定制板块Ids 查询哪些关联了定制计划
     *
     * @param sectionIds 定制板块id集合
     * @return 关联信息集合
     */
    List<Map<String, Object>> findBySectionIdsAndPlanIds(@Param("sectionIds") String sectionIds, @Param("planIds") String planIds);

    /**
     * 查询定制计划与版块关系记录
     *
     * @param sectionId 定制版块ID
     * @param planIds 定制计划ID集合
     * @return 关系集合
     */
    List<CustomPlanSectionRelation> selectRelationListBySectionId(@Param("sectionId") Long sectionId, @Param("planIds") String planIds);

    /**
     * 查询定制计划与版块关系记录
     *
     * @param planId 定制计划ID
     * @param sectionIds 定制版块ID集合
     * @return 关系集合
     */
    List<CustomPlanSectionRelation> selectRelationListByPlanId(@Param("planId") Long planId, @Param("sectionIds") List<Long> sectionIds);

}
