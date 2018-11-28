package com.lhiot.healthygood.service.customplan;

import com.leon.microx.util.BeanUtils;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.lhiot.healthygood.domain.customplan.CustomPlan;
import com.lhiot.healthygood.domain.customplan.CustomPlanParam;
import com.lhiot.healthygood.domain.customplan.CustomPlanSection;
import com.lhiot.healthygood.mapper.customplan.CustomPlanMapper;
import com.lhiot.healthygood.mapper.customplan.CustomPlanSectionMapper;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanDetailResult;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanSectionResult;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanSimpleResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.*;

@Service
@Transactional
public class CustomPlanService {
    private final CustomPlanSectionMapper customPlanSectionMapper;
    private final CustomPlanMapper customPlanMapper;


    @Autowired
    public CustomPlanService(CustomPlanSectionMapper customPlanSectionMapper,CustomPlanMapper customPlanMapper ) {
        this.customPlanSectionMapper = customPlanSectionMapper;
        this.customPlanMapper = customPlanMapper;
    }
    public List<CustomPlanSectionResult> findComPlanSectionByCode(String code) {
        List<CustomPlanSectionResult> result = new ArrayList<>();
        List<CustomPlanSection> customPlanSections = customPlanSectionMapper.selectBySectionCode(code);
        for(CustomPlanSection customPlanSection:customPlanSections){
            result.add(getCustomPlanSecionResult(customPlanSection));
        }
        return result;
    }
    CustomPlanSectionResult getCustomPlanSecionResult(CustomPlanSection customPlanSection){
        CustomPlanSectionResult customPlanSectionResult = new CustomPlanSectionResult();
        if(null != customPlanSection ){
            BeanUtils.of(customPlanSectionResult).populate(customPlanSection);
            customPlanSectionResult.setImage(customPlanSection.getSectionImage());
            List<CustomPlanSimpleResult> customPlanSimpleResults = new ArrayList<>();
            List<CustomPlan> customPlanList = customPlanMapper.findByCustomPlanSectionId(customPlanSection.getId());
            if(CollectionUtils.isEmpty(customPlanList)){
                for(CustomPlan customPlan:customPlanList){
                    CustomPlanSimpleResult customPlanSimpleResult = new CustomPlanSimpleResult();
                    BeanUtils.of(customPlanSimpleResult).populate(customPlan);
                    customPlanSimpleResults.add(customPlanSimpleResult);
                }
            }
            customPlanSectionResult.setCustomPlanList(customPlanSimpleResults);
        }
        //查询板块的定制计划
        return customPlanSectionResult;
    }

    public CustomPlanSectionResult findComPlanSectionId(Long id) {
        CustomPlanSection customPlanSection = customPlanSectionMapper.selectById(id);
        return getCustomPlanSecionResult(customPlanSection);
    }

    public CustomPlanDetailResult findDetail(Long id) {
        CustomPlanDetailResult result = new CustomPlanDetailResult();
        CustomPlan customPlan = customPlanMapper.selectById(id);
        BeanUtils.of(result).populate(customPlan);

        return null;
    }


    /**
     * 根据条件查询定制计划信息列表
     *
     * @param param 参数
     * @return 定制计划信息列表
     */
    public Pages<CustomPlan> findList(CustomPlanParam param) {
        List<CustomPlan> list = customPlanMapper.pageCustomPlans(param);
        boolean pageFlag = Objects.nonNull(param.getPage()) && Objects.nonNull(param.getRows()) && param.getPage() > 0 && param.getRows() > 0;
        int total = pageFlag ? customPlanMapper.pageCustomPlanCounts(param) : list.size();
        return Pages.of(total, list);
    }

    /**
     * 新增定制计划
     *
     * @param customPlan
     * @return Tips信息  成功在message中返回Id
     */
    public Tips addCustomPlan(CustomPlan customPlan){
        if (Objects.isNull(customPlan.getName())) {
            return Tips.warn("定制计划名称为空，添加失败.");
        }
        // 幂等添加
        CustomPlan customPlan1 = customPlanMapper.selectByName(customPlan.getName());
        if (Objects.nonNull(customPlan1)) {
            return Tips.warn("定制计划名称重复，添加失败");
        }
        customPlan.setCreateAt(Date.from(Instant.now()));
        customPlanMapper.create(customPlan);
        return Tips.info(customPlan.getId() + "");
    }

    /**
     * 修改定制计划
     *
     * @param customPlan
     * @return
     */
    public boolean update(CustomPlan customPlan) {
        return customPlanMapper.updateById(customPlan) > 0;
    }

    /**
     * 根据id查找单个定制计划
     *
     * @param id
     * @return
     */
    public CustomPlan findById(Long id) {
        return customPlanMapper.selectById(id);
    }


    /**
     * 根据id批量删除
     * @param ids
     * @return
     */
    public boolean batchDeleteByIds(String ids) {
        return customPlanMapper.deleteByIds(Arrays.asList(ids.split(","))) > 0;
    }
}
