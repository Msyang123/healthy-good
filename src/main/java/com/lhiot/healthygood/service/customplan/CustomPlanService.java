package com.lhiot.healthygood.service.customplan;

import com.leon.microx.util.BeanUtils;
import com.lhiot.healthygood.domain.customplan.CustomPlan;
import com.lhiot.healthygood.domain.customplan.CustomPlanSection;
import com.lhiot.healthygood.mapper.customplan.CustomPlanMapper;
import com.lhiot.healthygood.mapper.customplan.CustomPlanSectionMapper;
import com.lhiot.healthygood.domain.customplan.CustomPlanDetailResult;
import com.lhiot.healthygood.domain.customplan.CustomPlanSectionResult;
import com.lhiot.healthygood.domain.customplan.CustomPlanSimpleResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

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
}
