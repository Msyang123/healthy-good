package com.lhiot.healthygood.service.customplan;

import com.leon.microx.util.BeanUtils;
import com.leon.microx.util.StringUtils;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.lhiot.healthygood.domain.customplan.*;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanDetailResult;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanSectionResult;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanSimpleResult;
import com.lhiot.healthygood.mapper.customplan.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class CustomPlanService {
    private final CustomPlanSectionMapper customPlanSectionMapper;
    private final CustomPlanMapper customPlanMapper;
    private final CustomPlanSectionRelationMapper customPlanSectionRelationMapper;
    private final CustomPlanSpecificationMapper customPlanSpecificationMapper;
    private final CustomPlanProductMapper customPlanProductMapper;


    @Autowired
    public CustomPlanService(CustomPlanSectionMapper customPlanSectionMapper, CustomPlanMapper customPlanMapper, CustomPlanSectionRelationMapper customPlanSectionRelationMapper, CustomPlanSpecificationMapper customPlanSpecificationMapper, CustomPlanProductMapper customPlanProductMapper) {
        this.customPlanSectionMapper = customPlanSectionMapper;
        this.customPlanMapper = customPlanMapper;
        this.customPlanSectionRelationMapper = customPlanSectionRelationMapper;
        this.customPlanSpecificationMapper = customPlanSpecificationMapper;
        this.customPlanProductMapper = customPlanProductMapper;
    }

    public List<CustomPlanSectionResult> findComPlanSectionByCode(String code) {
        List<CustomPlanSectionResult> result = new ArrayList<>();
        List<CustomPlanSection> customPlanSections = customPlanSectionMapper.selectBySectionCode(code);
        for (CustomPlanSection customPlanSection : customPlanSections) {
            result.add(getCustomPlanSecionResult(customPlanSection));
        }
        return result;
    }

    CustomPlanSectionResult getCustomPlanSecionResult(CustomPlanSection customPlanSection) {
        CustomPlanSectionResult customPlanSectionResult = new CustomPlanSectionResult();
        if (null != customPlanSection) {
            BeanUtils.of(customPlanSectionResult).populate(customPlanSection);
            customPlanSectionResult.setImage(customPlanSection.getSectionImage());
            List<CustomPlanSimpleResult> customPlanSimpleResults = new ArrayList<>();
            List<CustomPlan> customPlanList = customPlanMapper.findByCustomPlanSectionId(customPlanSection.getId());
            if (CollectionUtils.isEmpty(customPlanList)) {
                for (CustomPlan customPlan : customPlanList) {
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

        return result;
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
     * @param customPlanResult
     * @return Tips信息  成功在message中返回Id
     */
    // FIXME   TCC操作
    public Tips addCustomPlan(CustomPlanResult customPlanResult) {
        // 幂等添加 定制计划
        CustomPlan customPlan1 = customPlanMapper.selectByName(customPlanResult.getName());
        if (Objects.nonNull(customPlan1)) {
            return Tips.warn("定制计划名称重复，添加失败");
        }
        customPlanResult.setCreateAt(Date.from(Instant.now()));
        CustomPlan customPlan = new CustomPlan();
        BeanUtils.copyProperties(customPlanResult, customPlan);
        boolean addCustomPlan = customPlanMapper.create(customPlan) > 0;

        if (!addCustomPlan) {
            return Tips.warn("添加定制计划失败");
        }
        // 新增的定制计划id
        Long customPlanId = customPlan.getId();

        // 幂等添加 定制板块和定制计划的关联
        List<CustomPlanSectionRelation> customPlanSectionRelations = customPlanSectionRelationMapper.selectRelationListByPlanId(customPlanId, customPlanResult.getCustomPlanSectionIds());
        if (!customPlanSectionRelations.isEmpty()) {
            return Tips.warn("定制计划与版块关联重复，添加失败");
        }
        List<CustomPlanSectionRelation> customPlanSectionRelation = new ArrayList<>();
        String[] sectionIds = StringUtils.tokenizeToStringArray(customPlanResult.getCustomPlanSectionIds(), ",");
        List<String> sectionIdList = Stream.of(sectionIds).collect(Collectors.toList());
        String[] sortIds = StringUtils.tokenizeToStringArray(customPlanResult.getSorts(), ",");
        List<String> sortIdList = Stream.of(sortIds).collect(Collectors.toList());
        if (sectionIdList.isEmpty() || sortIdList.isEmpty() || sectionIdList.size() != sortIdList.size()) {
            return Tips.warn("关联板块ids和排序ids长度不一致");
        }
        sectionIdList.stream().forEach(s -> {
            CustomPlanSectionRelation customPlanSectionRelation1 = new CustomPlanSectionRelation();
            customPlanSectionRelation1.setPlanId(customPlanId);
            customPlanSectionRelation1.setSectionId(Long.valueOf(s));
            customPlanSectionRelation1.setSort(Long.valueOf(sortIdList.get(sectionIdList.indexOf(s))));
            customPlanSectionRelation.add(customPlanSectionRelation1);
        });
        boolean addRelation = customPlanSectionRelationMapper.insertList(customPlanSectionRelation) > 0;

        if (!addRelation) {
            return Tips.warn("定制计划和定制板块关联失败");
        }
        // 添加定制计划规格  FIXME (定制规格和定制商品需要幂等添加？)
        List<CustomPlanSpecification> customPlanSpecifications = customPlanResult.getCustomPlanSpecifications();
        if (addCustomPlan && customPlanSpecifications != null && !customPlanSpecifications.isEmpty()) {
            customPlanSpecifications = customPlanSpecifications.stream()
                    .peek(specification -> specification.setPlanId(customPlanId))
                    .collect(Collectors.toList());
            boolean addCustomPlanSpecification = customPlanSpecificationMapper.insertList(customPlanSpecifications) > 0;
            if (!addCustomPlanSpecification) {
                return Tips.warn("定制计划规格添加失败");
            }
            // 添加定制计划商品 TODO 新增定制周期和定制序号
            customPlanSpecifications.stream()
                    .forEach(customPlanSpecification -> {
                        List<CustomPlanProduct> customPlanProducts = customPlanSpecification.getCustomPlanProducts().stream()
                                .peek(customPlanProduct -> customPlanProduct.setPlanId(customPlanId)).collect(Collectors.toList());
                        boolean addCustomPlanProduct = customPlanProductMapper.insertList(customPlanProducts) > 0;
                        if (!addCustomPlanProduct) {
                            Tips.warn("定制计划商品添加失败");
                        }
                        // FIXME ruturn Tips.warn("定制计划商品添加失败");
                    });
        }
        return Tips.info(customPlanId + "");
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
     * 修改定制计划商品
     *
     * @param customPlanProducts
     * @return
     */
    public Tips updateProduct(List<CustomPlanProduct> customPlanProducts) {
        // 循环批量更新上架商品id
        customPlanProducts.stream().forEach(customPlanProduct -> {
            boolean addProduct = customPlanProductMapper.updateById(customPlanProduct) > 0;
            if (!addProduct){
                Tips.warn("修改失败");
            }
        });
        return Tips.info("修改成功");
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
     *
     * @param ids
     * @return
     */
    public boolean batchDeleteByIds(String ids) {
        return customPlanMapper.deleteByIds(Arrays.asList(ids.split(","))) > 0;
    }
}
