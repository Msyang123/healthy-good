package com.lhiot.healthygood.service.customplan;

import com.leon.microx.predefine.OnOff;
import com.leon.microx.util.Beans;
import com.leon.microx.util.StringUtils;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.lhiot.dc.dictionary.DictionaryClient;
import com.lhiot.dc.dictionary.module.Dictionary;
import com.lhiot.healthygood.domain.customplan.*;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanDetailResult;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanParam;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanPeriodResult;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanProductResult;
import com.lhiot.healthygood.feign.BaseDataServiceFeign;
import com.lhiot.healthygood.feign.model.Product;
import com.lhiot.healthygood.feign.model.ProductShelf;
import com.lhiot.healthygood.feign.model.ProductShelfParam;
import com.lhiot.healthygood.mapper.customplan.*;
import com.lhiot.healthygood.type.OptionType;
import com.lhiot.healthygood.util.FeginResponseTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomPlanService {
    private final CustomPlanMapper customPlanMapper;
    private final CustomPlanSpecificationMapper customPlanSpecificationMapper;
    private final CustomPlanSectionRelationMapper customPlanSectionRelationMapper;
    private final BaseDataServiceFeign baseDataServiceFeign;
    private final CustomPlanProductMapper customPlanProductMapper;
    private DictionaryClient dictionaryClient;
    private final CustomPlanSpecificationStandardMapper customPlanSpecificationStandardMapper;


    @Autowired
    public CustomPlanService(CustomPlanMapper customPlanMapper,
                             CustomPlanSpecificationMapper customPlanSpecificationMapper, CustomPlanSectionRelationMapper customPlanSectionRelationMapper,
                             BaseDataServiceFeign baseDataServiceFeign, CustomPlanProductMapper customPlanProductMapper, DictionaryClient dictionaryClient, CustomPlanSpecificationStandardMapper customPlanSpecificationStandardMapper) {
        this.customPlanMapper = customPlanMapper;
        this.customPlanSpecificationMapper = customPlanSpecificationMapper;
        this.customPlanSectionRelationMapper = customPlanSectionRelationMapper;
        this.baseDataServiceFeign = baseDataServiceFeign;
        this.customPlanProductMapper = customPlanProductMapper;
        this.dictionaryClient = dictionaryClient;
        this.customPlanSpecificationStandardMapper = customPlanSpecificationStandardMapper;
    }

    public CustomPlanDetailResult findDetail(Long id) {
        CustomPlanDetailResult result = new CustomPlanDetailResult();
        CustomPlan customPlan = customPlanMapper.selectById(id);
        if (Objects.isNull(customPlan)) {
            return result;
        }
        //BeanUtils.copyProperties(customPlan, result);
        Beans.from(customPlan).to(result);
        List<CustomPlanPeriodResult> customPlanPeriodResultList = getCustomPlanPeriodResultList(id);
        result.setPeriodList(customPlanPeriodResultList);
        // 最低定制规格价格
        result.setPrice(customPlanSpecificationMapper.findMinPriceByPlanId(id));
        // 定制计划关联的定制板块
        List<CustomPlanSectionRelation> customPlanSectionRelationList = customPlanSectionRelationMapper.findByPlanId(id);
        if (!CollectionUtils.isEmpty(customPlanPeriodResultList)) {
            List<Long> sectionIdList = customPlanSectionRelationList.stream().map(CustomPlanSectionRelation::getSectionId).collect(Collectors.toList());
            result.setCustomPlanSectionIds(sectionIdList);
        }
        return result;
    }

    private List<CustomPlanPeriodResult> getCustomPlanPeriodResultList(Long customPlanId) {
        //获取定制计划周期 - 周
        List<CustomPlanPeriodResult> results = new ArrayList<>();
        CustomPlanPeriodResult customPlanPeriodOfWeekResult = getCustomPlanDetailStandardResult(customPlanId, 7);
        CustomPlanPeriodResult customPlanPeriodOfMonthResult = getCustomPlanDetailStandardResult(customPlanId, 30);
        if(Objects.nonNull(customPlanPeriodOfWeekResult.getProducts()) && customPlanPeriodOfWeekResult.getProducts().size()>0) {
            results.add(customPlanPeriodOfWeekResult);
        }
        if(Objects.nonNull(customPlanPeriodOfMonthResult.getProducts()) && customPlanPeriodOfMonthResult.getProducts().size()>0) {
            results.add(customPlanPeriodOfMonthResult);
        }
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
        Object[] shelfIds = customPlanProducts.parallelStream().map(CustomPlanProduct::getProductShelfId).map(String::valueOf).toArray(Object[]::new);

        ProductShelfParam productShelfParam = new ProductShelfParam();
        productShelfParam.setIds(StringUtils.arrayToCommaDelimitedString(shelfIds));
        productShelfParam.setShelfStatus(OnOff.ON);
        productShelfParam.setIncludeProduct(true);
        //查找基础服务上架商品信息
        Tips<Pages<ProductShelf>> productShelfTips = FeginResponseTools.convertResponse(baseDataServiceFeign.searchProductShelves(productShelfParam));
        //如果查询失败直接不返回基础数据信息
        if (productShelfTips.err()) {
            customPlanProducts.forEach(customPlanProduct -> {
                CustomPlanProductResult customPlanProductResult = new CustomPlanProductResult();
                //BeanUtils.copyProperties(customPlanProduct, customPlanPeriodResult);
                Beans.from(customPlanProduct).to(customPlanPeriodResult);
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
                    //BeanUtils.copyProperties(customPlanProduct, customPlanProductResult);
                    Beans.from(customPlanProduct).to(customPlanProductResult);
                    customPlanProductResult.setImage(item.getImage());//设置上架图
                    customPlanProductResult.setProductName(item.getName());//设置上架名称
                    customPlanProductResult.setProductShelfId(item.getShelfId());
                    customPlanProductResult.setDescription(item.getDescription());
                    if (Objects.nonNull(item.getProductSpecification())) {
                        Tips<Product> productTips = FeginResponseTools.convertResponse(baseDataServiceFeign.single(item.getProductSpecification().getProductId()));//查询商品益处
                        if (!productTips.err()) {
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

    public CustomPlanAndSpecification findCustomPlanSpecificationDetail(Long id) {
        CustomPlanAndSpecification customPlanAndSpecification = new CustomPlanAndSpecification();
        customPlanAndSpecification.setCustomPlanSpecification(customPlanSpecificationMapper.selectById(id));
        customPlanAndSpecification.setCustomPlan(customPlanMapper.selectById(customPlanAndSpecification.getCustomPlanSpecification().getPlanId()));
        return customPlanAndSpecification;
    }

    public Optional<Dictionary> dictionaryOptional(String code) {
        Optional<Dictionary> optional = dictionaryClient.dictionary(code);
        return optional;
    }


    /**
     * 根据条件查询定制计划信息列表
     *
     * @param param 参数
     * @return 定制计划信息列表
     */
    public Pages<CustomPlanDetailResult> findList(CustomPlanParam param) {
        List<CustomPlanDetailResult> customPlanDetailResultList = new ArrayList<>();
        // 查询定制计划信息
        List<CustomPlan> customPlanList = customPlanMapper.pageCustomPlans(param);
        boolean pageFlag = Objects.nonNull(param.getPage()) && Objects.nonNull(param.getRows()) && param.getPage() > 0 && param.getRows() > 0;
        int total = pageFlag ? customPlanMapper.pageCustomPlanCounts(param) : customPlanList.size();
        if (CollectionUtils.isEmpty(customPlanList)) {
            return Pages.of(total, customPlanDetailResultList);
        }

        // 根据定制计划ids查询定制规格信息
        List<Long> planIdList = customPlanList.stream().map(CustomPlan::getId).collect(Collectors.toList());
        List<CustomPlanSpecification> customPlanSpecificationList = customPlanSpecificationMapper.findByPlanIds(planIdList);

        // 定制计划信息列表设值
        customPlanList.forEach(customPlan -> {
            CustomPlanDetailResult customPlanDetailResult = new CustomPlanDetailResult();
            // BeanUtils.of(customPlanDetailResult).populate(customPlan);
            Beans.from(customPlan).to(customPlanDetailResult);
            if (!CollectionUtils.isEmpty(customPlanSpecificationList)) {
                List<CustomPlanSpecification> customPlanSpecifications = customPlanSpecificationList.stream().filter(customPlanSpecification -> Objects.equals(customPlanDetailResult.getId(), customPlanSpecification.getPlanId())).collect(Collectors.toList());
                // 周期集合
                List<CustomPlanPeriodResult> customPlanPeriodResultList = new ArrayList<>();

                List<Integer> planPeriodList = customPlanSpecifications.stream().map(CustomPlanSpecification::getPlanPeriod).distinct().collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(planPeriodList)) {
                    planPeriodList.forEach(planPeriod -> {
                        // 周期类型对象
                        CustomPlanPeriodResult customPlanPeriodResult = new CustomPlanPeriodResult();
                        // 设置周期类型-周期关联板块ids和排序ids长度不一致
                        customPlanPeriodResult.setPlanPeriod(planPeriod);
                        List<CustomPlanSpecification> filterCustomPlanSpecificationList = customPlanSpecifications.stream().filter(customPlanSpecification -> Objects.equals(planPeriod, customPlanSpecification.getPlanPeriod())).collect(Collectors.toList());
                        // 设置周期类型-对应周期的定制规格集合
                        customPlanPeriodResult.setSpecificationList(filterCustomPlanSpecificationList);
                        // 将周期类型关联板块ids和排序ids长度不一致添加至周期集合
                        customPlanPeriodResultList.add(customPlanPeriodResult);
                        customPlanDetailResult.setPeriodList(customPlanPeriodResultList);
                    });
                }
            }
            customPlanDetailResultList.add(customPlanDetailResult);
        });

        return Pages.of(total, customPlanDetailResultList);
    }

    /**
     * 新增定制计划
     *
     * @param customPlanDetailResult
     * @return Tips信息  成功在message中返回Id
     */
    public Tips addCustomPlan(CustomPlanDetailResult customPlanDetailResult) {
        // 幂等添加 定制计划
        CustomPlan selectCustomPlan = customPlanMapper.selectByName(customPlanDetailResult.getName());
        if (Objects.nonNull(selectCustomPlan)) {
            return Tips.warn("定制计划名称重复，添加失败");
        }
        CustomPlan customPlan = new CustomPlan();
        Beans.from(customPlanDetailResult).to(customPlan);
        customPlan.setCreateAt(Date.from(Instant.now()));
        boolean addCustomPlan = customPlanMapper.create(customPlan) > 0;
        if (!addCustomPlan) {
            return Tips.warn("添加定制计划失败");
        }
        Long customPlanId = customPlan.getId();
        // 幂等添加 定制板块和定制计划的关联
        List<CustomPlanSectionRelation> customPlanSectionRelations = customPlanSectionRelationMapper.selectRelationListByPlanId(customPlanId, customPlanDetailResult.getCustomPlanSectionIds());
        if (!customPlanSectionRelations.isEmpty()) {
            return Tips.warn("定制计划与版块关联重复，添加失败");
        }
        List<CustomPlanSectionRelation> insertRelationList = new ArrayList<>();
        if (customPlanDetailResult.getCustomPlanSectionIds().isEmpty()) {
            return Tips.warn("关联板块不能为空");
        }
        customPlanDetailResult.getCustomPlanSectionIds().stream().forEach(sectionId -> {
            CustomPlanSectionRelation addCustomPlanSectionRelation = new CustomPlanSectionRelation();
            addCustomPlanSectionRelation.setPlanId(customPlanId);
            addCustomPlanSectionRelation.setSectionId(sectionId);
            addCustomPlanSectionRelation.setSort(1L);
            insertRelationList.add(addCustomPlanSectionRelation);
        });
        if (!CollectionUtils.isEmpty(insertRelationList)) {
            boolean addRelation = customPlanSectionRelationMapper.insertList(insertRelationList) > 0;
            if (!addRelation) {
                return Tips.warn("定制计划和定制板块关联失败");
            }
        }
        // 添加定制计划规格和定制计划商品
        List<CustomPlanSpecification> insertSpecificationList = new ArrayList<>();
        List<CustomPlanProduct> insertProductList = new ArrayList<>();
        List<CustomPlanPeriodResult> customPlanPeriodResultList = customPlanDetailResult.getPeriodList();
        List<CustomPlanSpecificationStandard> planSpecificationStandardList = customPlanSpecificationStandardMapper.findList();
        if (!CollectionUtils.isEmpty(customPlanPeriodResultList)) {
            customPlanPeriodResultList.forEach(customPlanPeriodResult -> {
                // 要添加的定制计划规格
                customPlanPeriodResult.getSpecificationList().forEach(specification -> {
                    specification.setPlanPeriod(customPlanPeriodResult.getPlanPeriod());
                    specification.setPlanId(customPlanId);
                    if (!CollectionUtils.isEmpty(planSpecificationStandardList)) {
                        planSpecificationStandardList.forEach(planSpecificationStandard -> {
                            if (Objects.equals(specification.getQuantity(), planSpecificationStandard.getQuantity())) {
                                specification.setDescription(planSpecificationStandard.getDescription());
                                specification.setImage(planSpecificationStandard.getImage());
                                specification.setStandardId(planSpecificationStandard.getId());
                            }
                        });
                    }
                    insertSpecificationList.add(specification);
                });
                // 要添加的定制计划商品
                customPlanPeriodResult.getProducts().forEach(productResult -> {
                    CustomPlanProduct planProduct = new CustomPlanProduct();
                    Beans.from(productResult).to(planProduct);
                    planProduct.setPlanPeriod(customPlanPeriodResult.getPlanPeriod());
                    planProduct.setPlanId(customPlanId);
                    planProduct.setSort(planProduct.getDayOfPeriod());
                    insertProductList.add(planProduct);
                });
            });
        }
        if (!CollectionUtils.isEmpty(insertSpecificationList)) {
            boolean addCustomPlanSpecification = customPlanSpecificationMapper.insertList(insertSpecificationList) > 0;
            if (!addCustomPlanSpecification) {
                return Tips.warn("定制计划规格添加失败");
            }
        }
        if (!CollectionUtils.isEmpty(insertProductList)) {
            boolean addCustomPlanProduct = customPlanProductMapper.insertList(insertProductList) > 0;
            if (!addCustomPlanProduct) {
                return Tips.warn("定制计划商品添加失败");
            }
        }
        return Tips.info(customPlanId + "");
    }

    /**
     * 修改定制计划
     *
     * @param id
     * @param customPlanDetailResult
     * @return
     */
    public Tips update(Long id, CustomPlanDetailResult customPlanDetailResult) {
        CustomPlan customPlan = new CustomPlan();
        //BeanUtils.copyProperties(customPlanDetailResult, customPlan);
        Beans.from(customPlanDetailResult).to(customPlan);
        customPlan.setId(id);
        customPlan.setCreateAt(Date.from(Instant.now()));
        boolean updateCustomPlan = customPlanMapper.updateById(customPlan) > 0;
        if (!updateCustomPlan) {
            return Tips.warn("修改定制计划失败");
        }
        return Tips.info("修改定制计划成功");
    }

    /**
     * 修改定制计划周期类型信息
     *
     * @param customPlanDetailResult
     * @return
     */
    public Tips updatePeriod(Long id, CustomPlanDetailResult customPlanDetailResult) {
        List<CustomPlanSpecification> updateSpecificationList = new ArrayList<>();
        List<CustomPlanSpecification> insertSpecificationList = new ArrayList<>();
        List<CustomPlanProduct> updateProductList = new ArrayList<>();
        List<CustomPlanProduct> insertProductList = new ArrayList<>();

        // 查询定制定制计划规格基础数据表
        List<CustomPlanSpecificationStandard> planSpecificationStandardList = customPlanSpecificationStandardMapper.findList();

        customPlanDetailResult.getPeriodList().forEach(periodResult -> {
            List<CustomPlanSpecification> specificationList = periodResult.getSpecificationList().stream().filter(specification -> Objects.nonNull(specification.getOptionType())).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(specificationList)) {
                specificationList.forEach(specification -> {
                    if (Objects.equals(OptionType.UPDATE, specification.getOptionType())) {
                        updateSpecificationList.add(specification);
                    } else if (Objects.equals(OptionType.INSERT, specification.getOptionType())) {
                        // 设置规格信息
                        planSpecificationStandardList.forEach(planSpecificationStandard -> {
                            if (Objects.equals(specification.getQuantity(), planSpecificationStandard.getQuantity())) {
                                specification.setPlanId(id);
                                specification.setDescription(planSpecificationStandard.getDescription());
                                specification.setImage(planSpecificationStandard.getImage());
                                specification.setStandardId(planSpecificationStandard.getId());
                            }
                        });
                        insertSpecificationList.add(specification);
                    }
                });
            }
            List<CustomPlanProductResult> productList = periodResult.getProducts().stream().filter(product -> Objects.nonNull(product.getOptionType())).collect(Collectors.toList());
            Integer planPeriod = periodResult.getPlanPeriod();
            if (!CollectionUtils.isEmpty(productList)) {
                productList.forEach(productResult -> {
                    // 类型转换
                    CustomPlanProduct product = new CustomPlanProduct();
                    Beans.from(productResult).to(product);
                    if (Objects.equals(OptionType.UPDATE, productResult.getOptionType())) {
                        updateProductList.add(product);
                    } else if (Objects.equals(OptionType.INSERT, productResult.getOptionType())) {
                        // 设置商品信息
                        product.setPlanId(id);
                        product.setPlanPeriod(planPeriod);
                        product.setSort(productResult.getDayOfPeriod());
                        insertProductList.add(product);
                    }
                });
            }

        });
        // 批量修改的规格
        if (!CollectionUtils.isEmpty(updateSpecificationList)) {
            int updateSpecification = customPlanSpecificationMapper.updateBatch(updateSpecificationList);
            if (updateSpecification <= 0) {
                return Tips.warn("批量修改的规格失败");
            }
        }
        // 批量新增的规格
        if (!CollectionUtils.isEmpty(insertSpecificationList)) {
            int insertSpecification = customPlanSpecificationMapper.insertList(insertSpecificationList);
            if (insertSpecification <= 0) {
                return Tips.warn("批量新增的规格失败");
            }
        }
        // 批量修改的商品
        if (!CollectionUtils.isEmpty(updateProductList)) {
            int updateProduct = customPlanProductMapper.updateBatch(updateProductList);
            if (updateProduct <= 0) {
                return Tips.warn("批量修改的商品失败");
            }
        }
        // 批量新增的商品
        if (!CollectionUtils.isEmpty(insertProductList)) {
            int insertProduct = customPlanProductMapper.insertList(insertProductList);
            if (insertProduct <= 0) {
                return Tips.warn("批量新增的商品失败");
            }
        }
        return Tips.info("修改定制计划周期类型信息成功");
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
        // 批量删除定制计划
        boolean deleteCustomPlan = customPlanMapper.deleteByIds(Arrays.asList(ids.split(","))) > 0;
        if (!deleteCustomPlan) {
            return Tips.warn("批量删除定制计划失败");
        }
        // 批量删除定制商品
        customPlanProductMapper.deleteByPlanIds(Arrays.asList(ids.split(",")));
        // 批量删除定制规格
        customPlanSpecificationMapper.deleteByPlanIds(Arrays.asList(ids.split(",")));
        // 批量删除定制计划与定制板块的关联
        customPlanSectionRelationMapper.deleteByPlanIds(Arrays.asList(ids.split(",")));
        return Tips.info("删除成功");
    }

    /**
     * 最大暂停天数
     *
     * @return
     */
    public Dictionary customPlanMaxPauseDay() {
        return dictionaryClient.dictionary("customPlanMaxPauseDay").get();
    }


    /**
     * 根据上架id查询关联的定制计划列表
     * @param shelfId   上架id
     * @return  定制计划列表
     */
    public List<CustomPlan> findListByShelfId(Long shelfId) {
        // 根据上架板块id查询定制商品中的定制计划ids
        List<CustomPlanProduct> customPlanProduct = customPlanProductMapper.selectByShelfId(shelfId);
        if (CollectionUtils.isEmpty(customPlanProduct)) {
            return null;
        }
        // 根据定制计划ids查询定制计划列表
        List<Long> planIdList = customPlanProduct.stream().map(CustomPlanProduct::getPlanId).distinct().collect(Collectors.toList());
        return customPlanMapper.selectByIds(planIdList);
    }
}
