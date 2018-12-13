package com.lhiot.healthygood.service.customplan;

import com.google.common.base.Joiner;
import com.leon.microx.util.StringUtils;
import com.leon.microx.web.result.Tips;
import com.lhiot.healthygood.domain.customplan.CustomPlanSectionRelation;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanSectionRelationResult;
import com.lhiot.healthygood.mapper.customplan.CustomPlanSectionRelationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Description:定制计划板块关联定制计划服务类
 *
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
    public Tips addRelation(CustomPlanSectionRelation customPlanSectionRelation) {
        // 幂等添加
        List<CustomPlanSectionRelation> customPlanSectionRelations = customPlanSectionRelationMapper.selectRelationListBySectionId(customPlanSectionRelation.getSectionId(), customPlanSectionRelation.getPlanId() + "");
        if (!customPlanSectionRelations.isEmpty()) {
            return Tips.warn("定制计划与版块关联重复，添加失败");
        }
        customPlanSectionRelationMapper.create(customPlanSectionRelation);
        return Tips.info(customPlanSectionRelation.getId() + "");
    }

    /**
     * 批量新增定制计划与版块关系
     *
     * @param sectionId 版块ID
     * @param planIds   定制计划ID集合
     * @return 执行结果
     */
    public Tips addRelationList(Long sectionId, String planIds, String sorts) {
        //先做幂等验证
        List<CustomPlanSectionRelation> relationList = customPlanSectionRelationMapper.selectRelationListBySectionId(sectionId, planIds);
        if (!relationList.isEmpty()) {
            return Tips.warn("定制计划与版块关联重复，添加失败");
        }
        List<CustomPlanSectionRelation> customPlanSectionRelationList = new ArrayList<>();
        // 获取计划id集合
        String[] planIdArrays = StringUtils.tokenizeToStringArray(planIds, ",");
        List<String> planIdList = Stream.of(planIdArrays).collect(Collectors.toList());
        // 获取排序集合
        String[] sortArrays = StringUtils.tokenizeToStringArray(sorts, ",");
        List<String> sortList = Stream.of(sortArrays).collect(Collectors.toList());
        // 如果计划id集合和排序id集合不为空，且两个集合相等，存入List<CustomPlanSectionRelation>中
        if (planIdList.isEmpty() || sortList.isEmpty() || planIdList.size() != sortList.size()) {
            return Tips.warn("定制计划id集合和排序集合要长度相等且不能为空");
        }
        planIdList.forEach(planId -> {
            CustomPlanSectionRelation customPlanSectionRelation = new CustomPlanSectionRelation();
            customPlanSectionRelation.setSectionId(sectionId);
            customPlanSectionRelation.setPlanId(Long.valueOf(planId));
            customPlanSectionRelation.setSort(Long.valueOf(sortList.get(planIds.indexOf(planId))));
            customPlanSectionRelationList.add(customPlanSectionRelation);
        });

        // 批量新增关联
        customPlanSectionRelationMapper.insertList(customPlanSectionRelationList);
        List<Long> idList = customPlanSectionRelationList.stream().map(CustomPlanSectionRelation::getId).collect(Collectors.toList());
        String ids = Joiner.on(",").join(idList);
        return Tips.info(ids);
    }

    /**
     * 批量新增定制计划与版块关系
     *
     * @param sectionId 版块ID
     * @param planIds   定制计划ID集合
     * @return 执行结果
     */
    public Tips updateRelationList(Long sectionId, String planIds, String sorts) {
        List<CustomPlanSectionRelation> customPlanSectionRelationList = new ArrayList<>();
        // 如果planIds为空，则只进行删除操作
        if (Objects.isNull(planIds)) {
            return customPlanSectionRelationMapper.deleteRelationList(sectionId, null) > 0 ? Tips.info("修改成功") : Tips.warn("修改失败");
        } else if (Objects.nonNull(planIds) && Objects.nonNull(sorts)) {
            // 如果planIds和sorts不为空，且长度相等，则只进行删除操作，再进行新增操作
            // 获取计划id集合
            String[] planIdArrays = StringUtils.tokenizeToStringArray(planIds, ",");
            List<String> planIdList = Stream.of(planIdArrays).collect(Collectors.toList());
            // 获取排序集合
            String[] sortArrays = StringUtils.tokenizeToStringArray(sorts, ",");
            List<String> sortList = Stream.of(sortArrays).collect(Collectors.toList());
            // 如果计划id集合和排序id集合不为空，且两个集合相等，存入List<CustomPlanSectionRelation>中
            if (planIdList.isEmpty() || sortList.isEmpty() || planIdList.size() != sortList.size()) {
                return Tips.warn("定制计划id集合和排序集合要长度相等且不能为空");
            }
            planIdList.forEach(planId -> {
                CustomPlanSectionRelation customPlanSectionRelation = new CustomPlanSectionRelation();
                customPlanSectionRelation.setSectionId(sectionId);
                customPlanSectionRelation.setPlanId(Long.valueOf(planId));
                customPlanSectionRelation.setSort(Long.valueOf(sortList.get(planIds.indexOf(planId))));
                customPlanSectionRelationList.add(customPlanSectionRelation);
            });

            // 先批量删除 定制计划id为空，则删除该板块id的所有关联关系
            boolean deleted = customPlanSectionRelationMapper.deleteRelationList(sectionId, null) > 0;
            if (!deleted) {
                Tips.warn("删除失败！");
            }
            // 再批量新增
            customPlanSectionRelationMapper.insertList(customPlanSectionRelationList);
            return Tips.info("修改成功");
        }
        return Tips.warn("修改失败");
    }


    /**
     * 删除定制计划与版块关系
     *
     * @param relationId 关系ID
     * @return 执行结果 true 或者 false
     */
    public boolean deleteRelation(String relationId) {
        return customPlanSectionRelationMapper.deleteByIds(Arrays.asList(relationId.split(","))) > 0;
    }


    /**
     * 批量删除定制计划与版块关系
     *
     * @param sectionId 定制版块ID
     * @param planIds   定制计划ID集合
     * @return 执行结果 true 或者 false
     */
    public boolean deleteRelationList(Long sectionId, String planIds) {
        return customPlanSectionRelationMapper.deleteRelationList(sectionId, planIds) > 0;
    }

    /**
     * 根据板块id查询关联的计划信息
     *
     * @param sectionId
     * @return
     */
    public List<CustomPlanSectionRelationResult> findPlanBySectionId(Long sectionId) {
        return Optional.of(customPlanSectionRelationMapper.findPlanBySectionId(sectionId)).orElse(Collections.emptyList());
    }

    /**
     * 根据传入板块ID集合，查询哪些关联了定制计划
     *
     * @param sectionIds 板块ID集合
     * @return 存在定制计划的板块id集合
     */
    public List<String> findBySectionIdsAndPlanIds(String sectionIds, String planIds) {
        List<String> resultList = new ArrayList<>();
        List<Map<String, Object>> relationList = customPlanSectionRelationMapper.findBySectionIdsAndPlanIds(sectionIds, planIds);
        // 获取定制计划id和板块id
        relationList.forEach(section -> resultList.add(section.get("sectionId").toString()));
        relationList.forEach(section -> resultList.add(section.get("planId").toString()));
        return resultList;
    }

}

