package com.lhiot.healthygood.service.customplan;

import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.lhiot.healthygood.domain.customplan.CustomPlan;
import com.lhiot.healthygood.domain.customplan.CustomPlanSection;
import com.lhiot.healthygood.domain.customplan.CustomPlanSectionParam;
import com.lhiot.healthygood.domain.customplan.CustomPlanSectionRelation;
import com.lhiot.healthygood.mapper.customplan.CustomPlanSectionMapper;
import com.lhiot.healthygood.mapper.customplan.CustomPlanSectionRelationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.Instant;
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
     * @param customPlanSection
     * @return
     */
    public Tips addCustomPlanSection(CustomPlanSection customPlanSection) {
        if (Objects.isNull(customPlanSection.getSectionCode())) {
            return Tips.warn("定制板块编码不能为空，添加失败！");
        }
        // 幂等添加
        List<CustomPlanSection> customPlanSection1 = customPlanSectionMapper.selectBySectionCode(customPlanSection.getSectionCode());
        if (customPlanSection1.isEmpty()) {
            return Tips.warn("定制板块编码重复，添加失败");
        }
        customPlanSection.setCreateAt(Date.from(Instant.now()));
        customPlanSectionMapper.create(customPlanSection);
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
    public boolean batchDeleteByIds(String ids) {
        return customPlanSectionMapper.deleteByIds(Arrays.asList(ids.split(","))) > 0;
    }

    /**
     * 根据id查找单个定制板块
     *
     * @param id
     * @return
     */
    public CustomPlanSection findById(Long id, boolean flag) {
        CustomPlanSection customPlanSection = customPlanSectionMapper.selectById(id);
        if (flag && Objects.nonNull(customPlanSection)) {
            List<CustomPlanSectionRelation> customPlanSectionRelation = customPlanSectionRelationMapper.findPlanBySectionId(id);
            if (customPlanSectionRelation.isEmpty()) {
                List<CustomPlan> customPlans = customPlanSectionRelation.stream().map(CustomPlanSectionRelation::getCustomPlan).collect(Collectors.toList());
                customPlanSection.setCustomPlanList(customPlans);
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
}