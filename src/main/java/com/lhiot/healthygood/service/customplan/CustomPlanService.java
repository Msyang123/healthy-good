package com.lhiot.healthygood.service.customplan;

import com.leon.microx.predefine.OnOff;
import com.leon.microx.util.BeanUtils;
import com.leon.microx.util.Maps;
import com.leon.microx.util.StringUtils;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.lhiot.healthygood.domain.customplan.CustomPlan;
import com.lhiot.healthygood.domain.customplan.CustomPlanProduct;
import com.lhiot.healthygood.domain.customplan.CustomPlanSectionRelation;
import com.lhiot.healthygood.domain.customplan.CustomPlanSpecification;
import com.lhiot.healthygood.domain.customplan.model.*;
import com.lhiot.healthygood.feign.BaseDataServiceFeign;
import com.lhiot.healthygood.feign.model.Product;
import com.lhiot.healthygood.feign.model.ProductShelf;
import com.lhiot.healthygood.feign.model.ProductShelfParam;
import com.lhiot.healthygood.mapper.customplan.CustomPlanMapper;
import com.lhiot.healthygood.mapper.customplan.CustomPlanProductMapper;
import com.lhiot.healthygood.mapper.customplan.CustomPlanSectionRelationMapper;
import com.lhiot.healthygood.mapper.customplan.CustomPlanSpecificationMapper;
import com.lhiot.healthygood.util.FeginResponseTools;
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
    private final CustomPlanMapper customPlanMapper;
    private final CustomPlanSpecificationMapper customPlanSpecificationMapper;
    private final CustomPlanSectionRelationMapper customPlanSectionRelationMapper;
    private final BaseDataServiceFeign baseDataServiceFeign;
    private final CustomPlanProductMapper customPlanProductMapper;


    @Autowired
    public CustomPlanService(CustomPlanMapper customPlanMapper,
                             CustomPlanSpecificationMapper customPlanSpecificationMapper, CustomPlanSectionRelationMapper customPlanSectionRelationMapper,
                             BaseDataServiceFeign baseDataServiceFeign, CustomPlanProductMapper customPlanProductMapper) {
        this.customPlanMapper = customPlanMapper;
        this.customPlanSpecificationMapper = customPlanSpecificationMapper;
        this.customPlanSectionRelationMapper = customPlanSectionRelationMapper;
        this.baseDataServiceFeign = baseDataServiceFeign;
        this.customPlanProductMapper = customPlanProductMapper;
    }

    public CustomPlanDetailResult findDetail(Long id) {
        CustomPlanDetailResult result = new CustomPlanDetailResult();
        CustomPlan customPlan = customPlanMapper.selectById(id);
        if(Objects.isNull(customPlan))
            return result;
        BeanUtils.copyProperties(customPlan, result);
        List<CustomPlanPeriodResult> customPlanPeriodResultList = getCustomPlanPeriodResultList(id);
        result.setCustomPlanPeriodResultList(customPlanPeriodResultList);
        result.setPrice(customPlanSpecificationMapper.findMinPriceByPlanId(id));//最低定制规格价格
        return result;
    }

    private List<CustomPlanPeriodResult> getCustomPlanPeriodResultList(Long customPlanId) {
        //获取定制计划周期 - 周
        List<CustomPlanPeriodResult> results = new ArrayList<>();
        CustomPlanPeriodResult customPlanPeriodOfWeekResult = getCustomPlanDetailStandardResult(customPlanId, 7);
        CustomPlanPeriodResult customPlanPeriodOfMonthResult = getCustomPlanDetailStandardResult(customPlanId, 30);
        results.add(customPlanPeriodOfWeekResult);
        results.add(customPlanPeriodOfMonthResult);
        return results;
    }

    private CustomPlanPeriodResult getCustomPlanDetailStandardResult(Long customPlanId, int type) {
        CustomPlanPeriodResult customPlanPeriodResult = new CustomPlanPeriodResult();
        customPlanPeriodResult.setPlanPeriod(type);
        //获取套餐列表 依据定制计划id和周期类型
        Map<String, Object> param = new HashMap<>();
        param.put("planId", customPlanId);
        param.put("planPeriod", type);
        List<CustomPlanSpecification> customPlanSpecifications = customPlanSpecificationMapper.findByPlanIdAndPerid(param);
        customPlanPeriodResult.setSpecificationList(customPlanSpecifications);
        //获取定制产品信息
        List<CustomPlanProduct> customPlanProducts = customPlanProductMapper.findByPlanIdAndPerid(param);
        List<CustomPlanProductResult> customPlanProductResults = new ArrayList<>();

        //依据上架ids查询上架商品信息
        Object[] shelfIds = customPlanProducts.parallelStream().map(CustomPlanProduct::getProductShelfId).map(String::valueOf).toArray(String[]::new);

        ProductShelfParam productShelfParam = new ProductShelfParam();
        productShelfParam.setIds(StringUtils.join(",", shelfIds));
        productShelfParam.setShelfStatus(OnOff.ON);
        //查找基础服务上架商品信息
        Tips<Pages<ProductShelf>> productShelfTips = FeginResponseTools.convertResponse(baseDataServiceFeign.searchProductShelves(productShelfParam));
        //如果查询失败直接不返回基础数据信息
        if (productShelfTips.err()) {
            customPlanProducts.forEach(customPlanProduct -> {
                CustomPlanProductResult customPlanProductResult = new CustomPlanProductResult();
                BeanUtils.copyProperties(customPlanProduct, customPlanProductResult);
                customPlanProductResults.add(customPlanProductResult);
            });
            customPlanPeriodResult.setProducts(customPlanProductResults);
            return customPlanPeriodResult;
        }
        //查询基础上架商品成功
        Pages<ProductShelf> productShelfPages = productShelfTips.getData();

        //设置定制商品中额外信息
        customPlanProducts.forEach(customPlanProduct -> productShelfPages.getArray().stream()
                //上架id相同的订单商品信息，通过基础服务获取的赋值给定制商品信息
                .filter(productShelf -> Objects.equals(customPlanProduct.getProductShelfId(), productShelf.getId()))
                .forEach(item -> {
                    CustomPlanProductResult customPlanProductResult = new CustomPlanProductResult();
                    BeanUtils.copyProperties(customPlanProduct, customPlanProductResult);
                    customPlanProductResult.setImage(item.getImage());//设置上架图

                    if(Objects.nonNull(item.getProductSpecification())) {
                        Tips<Product> productTips = FeginResponseTools.convertResponse(baseDataServiceFeign.single(item.getProductSpecification().getProductId()));//查询商品益处
                        if (!productTips.err()){
                            customPlanProductResult.setBenefit(productTips.getData().getBenefit());
                        }
                    }
                    customPlanProductResults.add(customPlanProductResult);
                })
        );
        //设置定制计划商品信息
        customPlanPeriodResult.setProducts(customPlanProductResults);
        return customPlanPeriodResult;
    }

    public CustomPlanSpecification findCustomPlanSpecificationDetail(Long id) {
        return customPlanSpecificationMapper.selectById(id);
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
            customPlanResult.setCustomPlanSpecifications(customPlanSpecification);
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
        List<CustomPlanSpecification> customPlanSpecificationResults = customPlanResult.getCustomPlanSpecifications();
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
            //TODO 不是在定制规格中添加定制商品 不是在规格中包含定制商品
/*            customPlanSpecificationResults.stream()
                    .forEach(customPlanSpecification -> {
                        List<CustomPlanProduct> customPlanProducts = customPlanSpecification.getCustomPlanProducts().stream()
                                .peek(customPlanProduct -> customPlanProduct.setPlanId(customPlanId)).collect(Collectors.toList());
                        boolean addCustomPlanProduct = customPlanProductMapper.insertList(customPlanProducts) > 0;
                        if (!addCustomPlanProduct) {
                            Tips.warn("定制计划商品添加失败");
                        }
                        // FIXME ruturn Tips.warn("定制计划商品添加失败");
                    });*/
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
