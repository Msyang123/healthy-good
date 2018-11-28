package com.lhiot.healthygood.service.customplan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.leon.microx.util.StringUtils;
import com.lhiot.healthygood.domain.customplan.CustomPlanSectionRelation;
import com.lhiot.healthygood.mapper.customplan.CustomPlanSectionRelationMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* Description:定制计划板块关联定制计划服务类
* @author hufan
* @date 2018/11/26
*/
@Service
@Transactional
public class CustomPlanSectionRelationService {

    private final CustomPlanSectionRelationMapper customPlanSectionRelationMapper;

    @Autowired
    public CustomPlanSectionRelationService(CustomPlanSectionRelationMapper customPlanSectionRelationMapper) {
        this.customPlanSectionRelationMapper = customPlanSectionRelationMapper;
    }

    /**
     * 新增定制计划与版块关系
     *
     * @param customPlanSectionRelation 定制计划与版块关系对象
     * @return 定制计划与版块关系Id
     */
    public Long addRelation(CustomPlanSectionRelation customPlanSectionRelation) {
        customPlanSectionRelationMapper.insert(customPlanSectionRelation);
        return customPlanSectionRelation.getId();
    }

    /**
     * 新增批量定制计划与版块关系
     *
     * @param sectionId 版块ID
     * @param planIds  定制计划ID集合
     * @return 执行结果
     */
    public boolean addRelationList(Long sectionId, String planIds, String sorts) {
        List<CustomPlanSectionRelation> psrList = new ArrayList<>();
        String[] planIdArrays = StringUtils.tokenizeToStringArray(planIds, ",");
        List<String> planIdList = Stream.of(planIdArrays).collect(Collectors.toList());
        String[] sortArrays = StringUtils.tokenizeToStringArray(sorts, ",");
        List<String> sortList = Stream.of(sortArrays).collect(Collectors.toList());
        CustomPlanSectionRelation customPlanSectionRelation;
        for (String planId : planIdList) {
            customPlanSectionRelation = new CustomPlanSectionRelation();
            customPlanSectionRelation.setSectionId(sectionId);
            customPlanSectionRelation.setPlanId(Long.valueOf(planId));
            for (String sort : sortList) {
                if (planIdList.indexOf(planId) == sortList.indexOf(sort)) {
                    customPlanSectionRelation.setSort(Long.valueOf(sort));
                }
            }
            psrList.add(customPlanSectionRelation);
        }
        return customPlanSectionRelationMapper.insertList(psrList) > 0;
    }


    /**
     * 删除定制计划与版块关系
     *
     * @param relationId 关系ID
     * @return 执行结果 true 或者 false
     */
    public boolean deleteRelation(Long relationId) {
        return customPlanSectionRelationMapper.deleteById(relationId) > 0;
    }


    /**
     * 批量删除定制计划与版块关系
     *
     * @param sectionId 定制版块ID
     * @param planIds  定制计划ID集合
     * @return 执行结果 true 或者 false
     */
    public boolean deleteRelationList(Long sectionId, String planIds) {
        return customPlanSectionRelationMapper.deleteRelationList(sectionId, planIds) > 0;
    }

    /**
     * 根据板块id查询关联的计划信息
     * @param sectionId
     * @return
     */
    public List<CustomPlanSectionRelation> findBySectionId(Long sectionId) {
        return customPlanSectionRelationMapper.findBySectionId(sectionId);
    }

    /**
     * 根据传入板块ID集合，查询关联的定制计划
     *
     * @param sectionIds 板块ID集合
     * @return 存在定制计划的板块id集合
     */
    public List<String> findBySectionIds(String sectionIds) {
        List<String> resultList = new ArrayList<>();
        List<Map<String, Object>> relationList = customPlanSectionRelationMapper.findBySectionIds(sectionIds);
        relationList.forEach(section -> resultList.add(section.get("sectionId").toString()));
        List<String> distinctList = resultList.stream().distinct().collect(Collectors.toList());
        return distinctList;
    }

}

