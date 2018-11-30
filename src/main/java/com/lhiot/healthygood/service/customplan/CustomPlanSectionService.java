package com.lhiot.healthygood.service.customplan;

import com.google.common.base.Joiner;
import com.leon.microx.util.BeanUtils;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.lhiot.healthygood.domain.customplan.CustomPlan;
import com.lhiot.healthygood.domain.customplan.CustomPlanSection;
import com.lhiot.healthygood.domain.customplan.CustomPlanSectionRelation;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanSectionParam;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanSectionRelationResult;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanSectionResultAdmin;
import com.lhiot.healthygood.mapper.customplan.CustomPlanSectionMapper;
import com.lhiot.healthygood.mapper.customplan.CustomPlanSectionRelationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author hufan created in 2018/11/27 10:44
 **/
@Service
@Transactional
public class CustomPlanSectionService {
    private final CustomPlanSectionMapper customPlanSectionMapper;
    private final CustomPlanSectionRelationMapper customPlanSectionRelationMapper;

    public CustomPlanSectionService(CustomPlanSectionMapper customPlanSectionMapper, CustomPlanSectionRelationMapper customPlanSectionRelationMapper) {
        this.customPlanSectionMapper = customPlanSectionMapper;
        this.customPlanSectionRelationMapper = customPlanSectionRelationMapper;
    }

    /**
     * 新增定制板块
     *
     * @param customPlanSectionResultAdmin
     * @return
     */
    // FIXME TCC事务回滚
    public Tips addCustomPlanSection(CustomPlanSectionResultAdmin customPlanSectionResultAdmin) {
        // 幂等添加
        List<CustomPlanSection> customPlanSection1 = customPlanSectionMapper.selectBySectionCode(customPlanSectionResultAdmin.getSectionCode());
        if (!customPlanSection1.isEmpty()) {
            return Tips.warn("定制板块编码重复，添加失败");
        }
        CustomPlanSection customPlanSection = new CustomPlanSection();
        BeanUtils.copyProperties(customPlanSectionResultAdmin, customPlanSection);
        customPlanSection.setCreateAt(Date.from(Instant.now()));
        boolean addCustomPlanSection = customPlanSectionMapper.create(customPlanSection) > 0;

        if (!addCustomPlanSection) {
            Tips.warn("添加定制板块失败！");
        }
        // 新增板块id
        Long sectionId = customPlanSection.getId();

        // 添加定制板块和定制计划的关联
        List<Long> planIds = customPlanSectionResultAdmin.getCustomPlanList().stream().map(CustomPlan::getId).collect(Collectors.toList());
        List<Long> sorts = customPlanSectionResultAdmin.getRelationSorts();
        if (Objects.nonNull(sectionId) && Objects.nonNull(planIds) && Objects.nonNull(sorts)) {
            //先做幂等验证
            List<CustomPlanSectionRelation> relationList = customPlanSectionRelationMapper.selectRelationListBySectionId(sectionId, Joiner.on(",").join(planIds));
            if (!relationList.isEmpty()) {
                Tips.warn("定制计划与版块关联重复，添加失败");
            }
            List<CustomPlanSectionRelation> customPlanSectionRelationList = new ArrayList<>();
            CustomPlanSectionRelation customPlanSectionRelation;

            // 如果计划id集合和排序id集合不为空，且两个集合相等，存入List<CustomPlanSectionRelation>中
            if (planIds.isEmpty() || sorts.isEmpty() || planIds.size() != sorts.size()) {
                return Tips.warn("定制计划id集合和排序集合要长度相等且不能为空");
            }
            for (Long planId : planIds) {
                customPlanSectionRelation = new CustomPlanSectionRelation();
                customPlanSectionRelation.setSectionId(sectionId);
                customPlanSectionRelation.setPlanId(planId);
                customPlanSectionRelation.setSort(sorts.get(planIds.indexOf(planId)));
                customPlanSectionRelationList.add(customPlanSectionRelation);
            }
            // 批量新增关联
            boolean addRelation = customPlanSectionRelationMapper.insertList(customPlanSectionRelationList) > 0;
            if (!addRelation) {
                Tips.warn("批量新增定制板块和定制计划的关联失败！");
            }
        }
        return Tips.info(customPlanSection.getId() + "");
    }

    /**
     * 修改定制板块
     *
     * @param customPlanSectionResultAdmin
     * @return
     */
    public boolean update(CustomPlanSectionResultAdmin customPlanSectionResultAdmin) {
        CustomPlanSection customPlanSection = new CustomPlanSection();
        BeanUtils.copyProperties(customPlanSectionResultAdmin, customPlanSection);
        return customPlanSectionMapper.updateById(customPlanSection) > 0;
    }

    /**
     * 删除定制板块
     *
     * @param ids
     * @return
     */
    public boolean batchDeleteByIds(String ids) {
        return customPlanSectionMapper.deleteByIds(Arrays.asList(ids.split(","))) > 0;
    }

    /**
     * 根据id查找单个定制板块
     *
     * @param id
     * @return
     */
    public CustomPlanSectionResultAdmin findById(Long id, boolean flag) {
        CustomPlanSection customPlanSection = customPlanSectionMapper.selectById(id);
        CustomPlanSectionResultAdmin customPlanSectionResultAdmin = new CustomPlanSectionResultAdmin();
        BeanUtils.copyProperties(customPlanSection, customPlanSectionResultAdmin);

        // 如果查询关联的定制计划信息
        if (flag && Objects.nonNull(customPlanSectionResultAdmin)) {
            List<CustomPlanSectionRelationResult> customPlanSectionRelationResults = customPlanSectionRelationMapper.findPlanBySectionId(id);
            if (!customPlanSectionRelationResults.isEmpty()) {
                List<CustomPlan> customPlans = customPlanSectionRelationResults.stream().map(CustomPlanSectionRelationResult::getCustomPlan).collect(Collectors.toList());
                List<Long> relationIds = customPlanSectionRelationResults.stream().map(CustomPlanSectionRelationResult::getSort).collect(Collectors.toList());
                customPlanSectionResultAdmin.setCustomPlanList(customPlans);
                customPlanSectionResultAdmin.setRelationSorts(relationIds);
            }
        }
        return customPlanSectionResultAdmin;
    }

    /**
     * 根据条件查询定制板块信息列表
     *
     * @param param 参数
     * @return 定制板块信息列表
     */
    public Pages<CustomPlanSection> findList(CustomPlanSectionParam param) {
        List<CustomPlanSection> list = customPlanSectionMapper.pageCustomPlanSections(param);
        boolean pageFlag = Objects.nonNull(param.getPage()) && Objects.nonNull(param.getRows()) && param.getPage() > 0 && param.getRows() > 0;
        int total = pageFlag ? customPlanSectionMapper.pageCustomPlanSectionCounts(param) : list.size();
        return Pages.of(total, list);
    }
}