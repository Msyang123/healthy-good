package com.lhiot.healthygood.service.customplan;

import com.leon.microx.util.BeanUtils;
import com.leon.microx.util.Maps;
import com.leon.microx.util.StringUtils;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.leon.microx.web.result.Tuple;
import com.lhiot.healthygood.domain.customplan.*;
import com.lhiot.healthygood.domain.customplan.model.*;
import com.lhiot.healthygood.feign.BaseDataServiceFeign;
import com.lhiot.healthygood.feign.model.ProductShelf;
import com.lhiot.healthygood.mapper.customplan.*;
import com.lhiot.healthygood.type.YesOrNo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class CustomPlanService {
    private final CustomPlanSectionMapper customPlanSectionMapper;
    private final CustomPlanMapper customPlanMapper;
    private final CustomPlanSpecificationMapper customPlanSpecificationMapper;
    private final CustomPlanSectionRelationMapper customPlanSectionRelationMapper;
    private final BaseDataServiceFeign baseDataServiceFeign;
    private final CustomPlanProductMapper customPlanProductMapper;


    @Autowired
    public CustomPlanService(CustomPlanSectionMapper customPlanSectionMapper, CustomPlanMapper customPlanMapper,
                             CustomPlanSpecificationMapper customPlanSpecificationMapper, CustomPlanSectionRelationMapper customPlanSectionRelationMapper,
                             BaseDataServiceFeign baseDataServiceFeign, CustomPlanProductMapper customPlanProductMapper) {
        this.customPlanSectionMapper = customPlanSectionMapper;
        this.customPlanMapper = customPlanMapper;
        this.customPlanSpecificationMapper = customPlanSpecificationMapper;
        this.customPlanSectionRelationMapper = customPlanSectionRelationMapper;
        this.baseDataServiceFeign = baseDataServiceFeign;
        this.customPlanProductMapper = customPlanProductMapper;
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
            setCustomPlanSecionPlanItems(customPlanSection, planSectionsParam);
        });
        return Tuple.of(customPlanSections);
    }

    /**
     * 设置定制计划板块的定制计划关联数据
     *
     * @param customPlanSection 当前定制板块
     * @param planSectionsParam 查询定制计划查询条件
     */
    void setCustomPlanSecionPlanItems(CustomPlanSection customPlanSection, PlanSectionsParam planSectionsParam) {
        if (null != customPlanSection) {
            List<CustomPlan> customPlanList = customPlanMapper.findByCustomPlanSectionId(planSectionsParam);
            customPlanSection.setCustomPlanList(Pages.of(Objects.isNull(customPlanList) ? 0 : customPlanList.size(), customPlanList));
        }
    }

    public CustomPlanSection findComPlanSectionId(PlanSectionsParam planSectionsParam) {
        CustomPlanSection customPlanSection = customPlanSectionMapper.selectById(planSectionsParam.getId());
        if (Objects.equals(planSectionsParam.getNeedChild(), YesOrNo.YES)) {
            setCustomPlanSecionPlanItems(customPlanSection, planSectionsParam);
        }
        return customPlanSection;
    }

    public CustomPlanDetailResult findDetail(Long id) {
        CustomPlanDetailResult result = new CustomPlanDetailResult();
        CustomPlan customPlan = customPlanMapper.selectById(id);
        BeanUtils.of(result).populate(customPlan);
        List<CustomPlanDatailStandardResult> customPlanDatailStandardResult = getCustomPlanDetailStandardResultList(customPlan);
        result.setStandardList(customPlanDatailStandardResult);
        return result;
    }

    private List<CustomPlanDatailStandardResult> getCustomPlanDetailStandardResultList(CustomPlan customPlan) {
        //获取定制计划周期 - 周
        List<CustomPlanDatailStandardResult> results = new ArrayList<>();
        CustomPlanDatailStandardResult customPlanDatailStandardResult = getCustomPlanDetailStandardResult(customPlan, "7");
        results.add(customPlanDatailStandardResult);
        return results;
    }

    private CustomPlanDatailStandardResult getCustomPlanDetailStandardResult(CustomPlan customPlan, String type) {
        CustomPlanDatailStandardResult customPlanDatailStandardResult = new CustomPlanDatailStandardResult();
        customPlanDatailStandardResult.setPlanPeriod(type);
        //获取套餐列表
        Map<String, Object> param = new HashMap<>();
        param.put("planId", customPlan.getId());
        param.put("planPeriod", type);
        List<CustomPlanSpecification> customPlanSpecifications = customPlanSpecificationMapper.findByPlanIdAndPerid(param);
        List<CustomPlanSpecificationResult> customPlanSpecificationResults = new ArrayList<>();
        for (CustomPlanSpecification customPlanSpecification : customPlanSpecifications) {
            CustomPlanSpecificationResult customPlanSpecificationResult = new CustomPlanSpecificationResult();
            BeanUtils.of(customPlanSpecificationResult).populate(customPlanSpecification);
            customPlanSpecificationResults.add(customPlanSpecificationResult);
        }
        customPlanDatailStandardResult.setSpecificationList(customPlanSpecificationResults);
        //获取定制产品信息
        List<CustomPlanProduct> customPlanProducts = customPlanProductMapper.findByPlanId(customPlan.getId());
        List<CustomPlanProductResult> customPlanProductResults = new ArrayList<>();
        for (CustomPlanProduct customPlanProduct : customPlanProducts) {
            CustomPlanProductResult customPlanProductResult = new CustomPlanProductResult();
            BeanUtils.of(customPlanProducts).populate(customPlanProduct);
            //查询上架规格信息
            Long productShelfId = customPlanProduct.getProductShelfId();
            ProductShelf productShelf = baseDataServiceFeign.singleShelf(productShelfId).getBody();
            BeanUtils.of(customPlanProductResult).populate(productShelf);
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

    /**
     * 根据条件查询定制计划信息列表
     *
     * @param param 参数
     * @return 定制计划信息列表
     */
    public Pages<CustomPlanResult> findList(CustomPlanParam param) {
        List<CustomPlanResult> customPlanResultList = new ArrayList<>();
        // 查询定制计划信息
        List<CustomPlan> customPlans = customPlanMapper.pageCustomPlans(param);
        boolean pageFlag = Objects.nonNull(param.getPage()) && Objects.nonNull(param.getRows()) && param.getPage() > 0 && param.getRows() > 0;
        int total = pageFlag ? customPlanMapper.pageCustomPlanCounts(param) : customPlans.size();

        customPlans.forEach(item -> {
            CustomPlanResult customPlanResult = new CustomPlanResult();
            BeanUtils.copyProperties(item, customPlanResult);
            // 查询定制计划规格信息
            List<CustomPlanSpecification> customPlanSpecification = customPlanSpecificationMapper.findByPlanIdAndPerid(Maps.of("planId", item.getId(), "planPeriod", null));
            // List<CustomPlanSpecification> 转换为 List<CustomPlanSpecificationResult> 并添加到结果中
            List<CustomPlanSpecificationResult> customPlanSpecificationResultList = new ArrayList<>();
            customPlanSpecification.forEach(specification -> {
                CustomPlanSpecificationResult customPlanSpecificationResult = new CustomPlanSpecificationResult();
                BeanUtils.copyProperties(specification, customPlanSpecificationResult);
                customPlanSpecificationResultList.add(customPlanSpecificationResult);
            });
            customPlanResult.setCustomPlanSpecifications(customPlanSpecificationResultList);
            customPlanResultList.add(customPlanResult);
        });
        return Pages.of(total, customPlanResultList);
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
        sectionIdList.stream().forEach(sectionId -> {
            CustomPlanSectionRelation customPlanSectionRelation1 = new CustomPlanSectionRelation();
            customPlanSectionRelation1.setPlanId(customPlanId);
            customPlanSectionRelation1.setSectionId(Long.valueOf(sectionId));
            customPlanSectionRelation1.setSort(Long.valueOf(sortIdList.get(sectionIdList.indexOf(sectionId))));
            customPlanSectionRelation.add(customPlanSectionRelation1);
        });
        boolean addRelation = customPlanSectionRelationMapper.insertList(customPlanSectionRelation) > 0;

        if (!addRelation) {
            return Tips.warn("定制计划和定制板块关联失败");
        }
        // 添加定制计划规格  FIXME (定制规格和定制商品需要幂等添加？)
        List<CustomPlanSpecificationResult> customPlanSpecificationResults = customPlanResult.getCustomPlanSpecifications();
        if (addCustomPlan && customPlanSpecificationResults != null && !customPlanSpecificationResults.isEmpty()) {
            customPlanSpecificationResults = customPlanSpecificationResults.stream()
                    .peek(specification -> specification.setPlanId(customPlanId))
                    .collect(Collectors.toList());
            List<CustomPlanSpecification> customPlanSpecificationList = new ArrayList<>();
            customPlanSpecificationResults.forEach(item -> {
                CustomPlanSpecification customPlanSpecification = new CustomPlanSpecification();
                BeanUtils.copyProperties(item, customPlanSpecification);
                customPlanSpecificationList.add(customPlanSpecification);
            });
            boolean addCustomPlanSpecification = customPlanSpecificationMapper.insertList(customPlanSpecificationList) > 0;
            if (!addCustomPlanSpecification) {
                return Tips.warn("定制计划规格添加失败");
            }
            // 添加定制计划商品 TODO 新增定制周期和定制序号
            customPlanSpecificationResults.stream()
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
    // FIXME 回滚操作
    public Tips updateProduct(List<CustomPlanProduct> customPlanProducts) {
        // 循环批量更新上架商品id
        customPlanProducts.stream().forEach(customPlanProduct -> {
            boolean addProduct = customPlanProductMapper.updateById(customPlanProduct) > 0;
            if (!addProduct) {
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
     * 根据定制计划id批量删除
     *
     * @param ids
     * @return
     */
    public Tips batchDeleteByIds(String ids) {
        // 定制计划是否关联了定制板块
        List<Map<String, Object>> relationList = customPlanSectionRelationMapper.findBySectionIdsAndPlanIds(null, ids);
        if (!relationList.isEmpty()) {
            List<String> resultList = new ArrayList<>();
            relationList.forEach(section -> resultList.add(section.get("planId").toString()));
            List<String> id = resultList.stream().distinct().collect(Collectors.toList());
            return Tips.warn("以下定制计划id不可删除:" + id);
        }
        return customPlanMapper.deleteByIds(Arrays.asList(ids.split(","))) > 0 ? Tips.info("删除成功") : Tips.warn("删除失败");
    }
}
