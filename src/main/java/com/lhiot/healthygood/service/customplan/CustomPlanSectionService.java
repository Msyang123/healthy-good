package com.lhiot.healthygood.service.customplan;

import com.google.common.base.Joiner;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.leon.microx.web.result.Tuple;
import com.lhiot.healthygood.domain.customplan.CustomPlan;
import com.lhiot.healthygood.domain.customplan.CustomPlanSection;
import com.lhiot.healthygood.domain.customplan.CustomPlanSectionRelation;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanSectionParam;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanSectionRelationResult;
import com.lhiot.healthygood.domain.customplan.model.PlanSectionsParam;
import com.lhiot.healthygood.mapper.customplan.CustomPlanMapper;
import com.lhiot.healthygood.mapper.customplan.CustomPlanSectionMapper;
import com.lhiot.healthygood.mapper.customplan.CustomPlanSectionRelationMapper;
import com.lhiot.healthygood.mapper.customplan.CustomPlanSpecificationMapper;
import com.lhiot.healthygood.type.YesOrNo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hufan created in 2018/11/27 10:44
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class CustomPlanSectionService {
    private final CustomPlanSectionMapper customPlanSectionMapper;
    private final CustomPlanSpecificationMapper customPlanSpecificationMapper;
    private final CustomPlanSectionRelationMapper customPlanSectionRelationMapper;
    private final CustomPlanMapper customPlanMapper;

    @Autowired
    public CustomPlanSectionService(CustomPlanSectionMapper customPlanSectionMapper, CustomPlanSpecificationMapper customPlanSpecificationMapper, CustomPlanSectionRelationMapper customPlanSectionRelationMapper, CustomPlanMapper customPlanMapper) {
        this.customPlanSectionMapper = customPlanSectionMapper;
        this.customPlanSpecificationMapper = customPlanSpecificationMapper;
        this.customPlanSectionRelationMapper = customPlanSectionRelationMapper;
        this.customPlanMapper = customPlanMapper;
    }

    /**
     * 新增定制板块
     *
     * @param customPlanSection
     * @return
     */
    public Tips addCustomPlanSection(CustomPlanSection customPlanSection) {
        // 幂等添加
        CustomPlanSection findCustomPlanSection = customPlanSectionMapper.selectBySectionCode(customPlanSection.getSectionCode());
        if (Objects.nonNull(findCustomPlanSection)) {
            return Tips.warn("定制板块编码重复，添加失败");
        }
        customPlanSection.setCreateAt(Date.from(Instant.now()));
        boolean addCustomPlanSection = customPlanSectionMapper.create(customPlanSection) > 0;
        if (!addCustomPlanSection) {
            return Tips.warn("添加定制板块失败！");
        }
        // 新增板块id
        Long sectionId = customPlanSection.getId();
        if (customPlanSection.getCustomPlanList().isEmpty() || customPlanSection.getCustomPlanList().size() == 0) {
            return Tips.info(customPlanSection.getId() + "");
        }
        // 添加定制板块和定制计划的关联
        List<Long> sorts = customPlanSection.getCustomPlanList().stream().map(CustomPlan::getRelationSort).collect(Collectors.toList());
        List<Long> planIds = customPlanSection.getCustomPlanList().stream().map(CustomPlan::getId).collect(Collectors.toList());
        if (Objects.nonNull(sectionId) && !planIds.isEmpty() && !sorts.isEmpty()) {
            //先做幂等验证
            List<CustomPlanSectionRelation> relationList = customPlanSectionRelationMapper.selectRelationListBySectionId(sectionId, Joiner.on(",").join(planIds));
            if (!relationList.isEmpty()) {
                return Tips.warn("定制计划与版块关联重复，添加失败");
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
                return Tips.warn("批量新增定制板块和定制计划的关联失败！");
            }
        }
        return Tips.info(customPlanSection.getId() + "");
    }

    /**
     * 修改定制板块
     *
     * @param customPlanSection
     * @return
     */
    public boolean update(CustomPlanSection customPlanSection) {
        return customPlanSectionMapper.updateById(customPlanSection) > 0;
    }

    /**
     * 删除定制板块
     *
     * @param ids
     * @return
     */
    public Tips batchDeleteByIds(String ids) {
        // 定制板块是否关联了定制计划
        return customPlanSectionMapper.deleteByIds(Arrays.asList(ids.split(","))) > 0 ? Tips.info("删除成功") : Tips.warn("删除失败");
    }

    /**
     * 根据id查找单个定制板块
     *
     * @param id
     * @return
     */
    public CustomPlanSection findById(Long id, boolean flag) {
        CustomPlanSection customPlanSection = customPlanSectionMapper.selectById(id);

        // 如果查询关联的定制计划信息
        if (flag && Objects.nonNull(customPlanSection)) {
            List<CustomPlanSectionRelationResult> customPlanSectionRelationResults = customPlanSectionRelationMapper.findPlanBySectionId(id);
            if (!customPlanSectionRelationResults.isEmpty()) {
                List<CustomPlan> customPlans = customPlanSectionRelationResults.stream().map(CustomPlanSectionRelationResult::getCustomPlan).collect(Collectors.toList());
                List<Long> relationSorts = customPlanSectionRelationResults.stream().map(CustomPlanSectionRelationResult::getSort).collect(Collectors.toList());
                List<Long> relationIds = customPlanSectionRelationResults.stream().map(CustomPlanSectionRelationResult::getId).collect(Collectors.toList());
                customPlanSection.setCustomPlanList(customPlans);
                customPlanSection.getCustomPlanList().stream().peek(customPlan -> {
                    customPlan.setRelationSort(relationSorts.get(customPlans.indexOf(customPlan)));
                    customPlan.setRelationId(relationIds.get(customPlans.indexOf(customPlan)));
                }).collect(Collectors.toList());
            }
        }
        return customPlanSection;
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


    /**
     * 定制首页
     *
     * @return
     */
    public Tuple<CustomPlanSection> customPlanSectionTuple() {
        //查询所有定制板块
        List<CustomPlanSection> customPlanSections = customPlanSectionMapper.customPlanSectionTuple();
        //找到定制板块下的定制计划（依据定制板块与定制计划关联表）
        customPlanSections.forEach(customPlanSection -> {
            //依据定制板块id查询下面定制计划列表
            PlanSectionsParam planSectionsParam = new PlanSectionsParam();
            planSectionsParam.setId(customPlanSection.getId());
            planSectionsParam.setPage(1);
            planSectionsParam.setRows(4);//只查询最多4个推荐定制计划
            setCustomPlanSectionPlanItems(customPlanSection, planSectionsParam);
        });
        return Tuple.of(customPlanSections);
    }

    /**
     * 设置定制计划板块的定制计划关联数据
     *
     * @param customPlanSection 当前定制板块
     * @param planSectionsParam 查询定制计划查询条件
     */
    void setCustomPlanSectionPlanItems(CustomPlanSection customPlanSection, PlanSectionsParam planSectionsParam) {
        if (null != customPlanSection) {
            List<CustomPlan> customPlanList = customPlanMapper.findByCustomPlanSectionId(planSectionsParam);
            customPlanList.forEach(item ->
                    item.setPrice(customPlanSpecificationMapper.findMinPriceByPlanId(item.getId()))
            );
            customPlanSection.setCustomPlanList(customPlanList);
        }
    }

    public CustomPlanSection findComPlanSectionId(PlanSectionsParam planSectionsParam) {
        CustomPlanSection customPlanSection = customPlanSectionMapper.selectById(planSectionsParam.getId());
        if (Objects.equals(planSectionsParam.getNeedChild(), YesOrNo.YES)) {
            setCustomPlanSectionPlanItems(customPlanSection, planSectionsParam);
        }
        return customPlanSection;
    }
}