package com.lhiot.healthygood.mapper.customplan;

import com.lhiot.healthygood.domain.customplan.CustomPlanSectionRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
* Description:定制计划板块关联定制计划Mapper类
* @author hufan
* @date 2018/11/26
*/
@Mapper
@Repository
public interface CustomPlanSectionRelationMapper {

    /**
     * 新增定制计划与版块关系记录
     *
     * @param customPlanSectionRelation 定制计划与版块关系对象
     * @return 执行结果
     */
    int insert(CustomPlanSectionRelation customPlanSectionRelation);


    /**
     * 新增批量定制计划与版块关系
     *
     * @param list 定制计划与版块关系集合
     * @return 执行结果
     */
    int insertList(List<CustomPlanSectionRelation> list);


    /**
     * 删除定制计划与版块关系记录
     *
     * @param relationId 关系ID
     * @return 执行结果
     */
    int deleteById(Long relationId);


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
     * 根据定制板块id 查询关联的定制计划信息
     * @param sectionId
     * @return
     */
    List<CustomPlanSectionRelation> findPlanBySectionId(@Param("sectionId") Long sectionId);

    /**
     * 根据定制板块Ids 查询哪些关联了定制计划
     *
     * @param sectionIds 定制板块id集合
     * @return 关联信息集合
     */
    List<Map<String, Object>> findBySectionIds(@Param("sectionIds") String sectionIds);

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
    List<CustomPlanSectionRelation> selectRelationListByPlanId(@Param("planId") Long planId, @Param("sectionIds") String sectionIds);

}
