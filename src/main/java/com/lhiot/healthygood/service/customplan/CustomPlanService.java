package com.lhiot.healthygood.service.customplan;

import com.leon.microx.util.BeanUtils;
import com.lhiot.healthygood.domain.customplan.CustomPlan;
import com.lhiot.healthygood.domain.customplan.CustomPlanSection;
import com.lhiot.healthygood.domain.customplan.CustomPlanSectionRelation;
import com.lhiot.healthygood.domain.customplan.CustomPlanSpecification;
import com.lhiot.healthygood.domain.customplan.model.*;
import com.lhiot.healthygood.domain.good.ProductShelf;
import com.lhiot.healthygood.feign.good.BaseDataServiceFeign;
import com.lhiot.healthygood.mapper.customplan.CustomPlanMapper;
import com.lhiot.healthygood.mapper.customplan.CustomPlanSectionMapper;
import com.lhiot.healthygood.mapper.customplan.CustomPlanSectionRelationMapper;
import com.lhiot.healthygood.mapper.customplan.CustomPlanSpecificationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CustomPlanService {
    private final CustomPlanSectionMapper customPlanSectionMapper;
    private final CustomPlanMapper customPlanMapper;
    private final CustomPlanSpecificationMapper customPlanSpecificationMapper;
    private final CustomPlanSectionRelationMapper customPlanSectionRelationMapper;
    private final BaseDataServiceFeign baseDataServiceFeign;

    @Autowired
    public CustomPlanService(CustomPlanSectionMapper customPlanSectionMapper,CustomPlanMapper customPlanMapper,
                             CustomPlanSpecificationMapper customPlanSpecificationMapper,CustomPlanSectionRelationMapper customPlanSectionRelationMapper,
                             BaseDataServiceFeign baseDataServiceFeign) {
        this.customPlanSectionMapper = customPlanSectionMapper;
        this.customPlanMapper = customPlanMapper;
        this.customPlanSpecificationMapper = customPlanSpecificationMapper;
        this.customPlanSectionRelationMapper =customPlanSectionRelationMapper;
        this.baseDataServiceFeign = baseDataServiceFeign;
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
        //
        List<CustomPlanDatailStandardResult> customPlanDatailStandardResult = getCustomPlanDetailStandardResultList(customPlan);
        //result.setStandardList();
        return null;
    }

    private List<CustomPlanDatailStandardResult> getCustomPlanDetailStandardResultList(CustomPlan customPlan) {
        //获取定制计划周期 - 周
        List<CustomPlanDatailStandardResult> results =  new ArrayList<>();
        CustomPlanDatailStandardResult customPlanDatailStandardResult = getCustomPlanDetailStandardResult(customPlan,"7");
        return null;
    }

    private CustomPlanDatailStandardResult getCustomPlanDetailStandardResult(CustomPlan customPlan, String type) {
        CustomPlanDatailStandardResult customPlanDatailStandardResult = new CustomPlanDatailStandardResult();
        customPlanDatailStandardResult.setPlanPeriod(type);
        //获取套餐列表
        Map<String,Object> param = new HashMap<String,Object>();
        param.put("planId",customPlan.getId());
        param.put("planPeriod",type);
        List<CustomPlanSpecification> customPlanSpecifications = customPlanSpecificationMapper.findByPlanIdAndPerid(param);
        List<CustomPlanSpecificationResult> customPlanSpecificationResults = new ArrayList<>();
        for(CustomPlanSpecification customPlanSpecification:customPlanSpecifications){
            CustomPlanSpecificationResult customPlanSpecificationResult = new CustomPlanSpecificationResult();
            BeanUtils.of(customPlanSpecificationResult).populate(customPlanSpecification);
            customPlanSpecificationResults.add(customPlanSpecificationResult);
        }
        customPlanDatailStandardResult.setSpecificationList(customPlanSpecificationResults);
        //获取定制产品信息
        List<CustomPlanSectionRelation> customPlanSectionRelations = customPlanSectionRelationMapper.findByPlanId(customPlan.getId());
        List<CustomPlanProductResult> customPlanProductResults = new ArrayList<>();
        for(CustomPlanSectionRelation customPlanSectionRelation:customPlanSectionRelations){
            CustomPlanProductResult customPlanProductResult = new CustomPlanProductResult();
            BeanUtils.of(customPlanProductResult).populate(customPlanSectionRelation);
            //查询上架规格信息
            Long productShelfId = customPlanProductResult.getProductShelfId();
            ProductShelf productShelf = baseDataServiceFeign.singleShelf(productShelfId).getBody();
            BeanUtils.of(productShelf).populate(customPlanSectionRelation);
            customPlanProductResults.add(customPlanProductResult);
           // customPlanSectionRelation.
        }
        customPlanDatailStandardResult.setProducts(customPlanProductResults);
        return customPlanDatailStandardResult;
    }

    public CustomPlanSpecificationDetailResult findCustomPlanSpecificationDetail(Long id) {
        CustomPlanSpecificationDetailResult customPlanSpecificationDetailResult = new CustomPlanSpecificationDetailResult();
        CustomPlanSpecification customPlanSpecification = customPlanSpecificationMapper.selectById(id);
        BeanUtils.of(customPlanSpecification).populate(customPlanSpecificationDetailResult);
        return customPlanSpecificationDetailResult;
    }
}
