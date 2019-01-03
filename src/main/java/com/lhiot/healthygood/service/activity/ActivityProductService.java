package com.lhiot.healthygood.service.activity;

import com.leon.microx.util.Beans;
import com.leon.microx.util.Maps;
import com.leon.microx.util.StringUtils;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.lhiot.healthygood.domain.activity.ActivityProduct;
import com.lhiot.healthygood.domain.activity.ActivitySectionRelation;
import com.lhiot.healthygood.domain.activity.model.ActivityProductParam;
import com.lhiot.healthygood.domain.activity.model.ActivityProductResult;
import com.lhiot.healthygood.feign.BaseDataServiceFeign;
import com.lhiot.healthygood.feign.model.ProductSectionRelation;
import com.lhiot.healthygood.feign.model.ProductShelf;
import com.lhiot.healthygood.feign.model.ProductShelfParam;
import com.lhiot.healthygood.feign.model.ProductSpecification;
import com.lhiot.healthygood.mapper.activity.ActivityProductMapper;
import com.lhiot.healthygood.mapper.activity.ActivitySectionRelationMapper;
import com.lhiot.healthygood.type.ActivityType;
import com.lhiot.healthygood.type.YesOrNo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
* Description:活动商品服务类
* @author yangjiawen
* @date 2018/11/24
*/
@Service
@Transactional(rollbackFor = Exception.class)
public class ActivityProductService {

    private final ActivityProductMapper activityProductMapper;
    private final ActivitySectionRelationMapper activitySectionRelationMapper;
    private final BaseDataServiceFeign baseDataServiceFeign;

    @Autowired
    public ActivityProductService(ActivityProductMapper activityProductMapper, ActivitySectionRelationMapper activitySectionRelationMapper, BaseDataServiceFeign baseDataServiceFeign) {
        this.activityProductMapper = activityProductMapper;
        this.activitySectionRelationMapper = activitySectionRelationMapper;
        this.baseDataServiceFeign = baseDataServiceFeign;
    }

    /**
     * Description:新增活动商品
     *
     * @param activityProduct
     * @return
     * @author hufan
     * @date 2018/11/24 16:09:12
     */
    public Tips create(ActivityProduct activityProduct) {
        activityProduct.setActivityType(ActivityType.NEW_SPECIAL.toString());
        // 幂等添加
        @NotNull(message = "新品尝鲜活动id不为空") Long specialProductActivityId = activityProduct.getSpecialProductActivityId();
        @NotNull(message = "商品上架id不为空") Long productShelfId = activityProduct.getProductShelfId();
        ActivityProduct findActivityProduct = activityProductMapper.selectBySpecialIdAndShelfId(specialProductActivityId, productShelfId);
        if (Objects.nonNull(findActivityProduct)) {
            return Tips.warn("该商品重复关联，添加失败");
        }
        // 添加活动商品
        boolean addActivityProduct = activityProductMapper.create(activityProduct) > 0;
        if (!addActivityProduct) {
            return Tips.warn("添加活动商品失败！");
        }
        // 添加上架商品与新品尝鲜板块的关联
        if (Objects.equals(YesOrNo.YES, activityProduct.getRelationSection())){
            // 根据活动id查询关联的板块id
            @NotNull(message = "活动id不为空") Long activityId = activityProduct.getActivityId();
            // 一个定制板块活动只会关联一个商品板块,如果有多个活动则要修改
            ActivitySectionRelation relation = activitySectionRelationMapper.selectRelation(Maps.of("activityId", activityId));
            // 根据板块id和上架id在基础服务中添加关联关系
            ResponseEntity entity = baseDataServiceFeign.create(new ProductSectionRelation(null, productShelfId, relation.getSectionId()));
            if (entity.getStatusCode().isError()) {
                return Tips.warn((String) entity.getBody());
            }
        }
        return Tips.info(activityProduct.getId() + "");
    }

    public List<ActivityProduct> list (ActivityProduct product){
        return activityProductMapper.selectActivityProducts(product);
    }

    /**
     * Description:根据id修改活动商品
     *
     * @param activityProduct
     * @return
     * @author hufan
     * @date 2018/11/24 16:09:12
     */
    public Tips updateById(Long id, ActivityProduct activityProduct) {
        // 修改活动商品
        activityProduct.setId(id);
        ActivityProduct findActivityProduct = activityProductMapper.selectById(id);
        Long beforeShelfId = findActivityProduct.getProductShelfId();

        boolean updateActivityProduct = activityProductMapper.updateById(activityProduct) > 0;
        if (!updateActivityProduct) {
            return Tips.warn("修改活动商品失败");
        }
        // 修改上架商品与新品尝鲜板块的关联
//        // 先删除 再新增
//        @NotNull(message = "活动id不为空") Long activityId = activityProduct.getActivityId();
//        // 一个定制板块活动只会关联一个商品板块,如果有多个活动则要修改
//        ActivitySectionRelation relation = activitydeleteBatchSectionRelationMapper.selectRelation(Maps.of("activityId", activityId));
//        ResponseEntity deleteEntity = baseDataServiceFeign.deleteBatch(relation.getSectionId(), beforeShelfId.toString());
//        if (deleteEntity.getStatusCode().isError()) {
//            return Tips.warn((String) deleteEntity.getBody());
//        }
//        ResponseEntity addEntity = baseDataServiceFeign.create(new ProductSectionRelation(null, relation.getSectionId(), activityProduct.getProductShelfId()));
//        if (addEntity.getStatusCode().isError()) {
//            return Tips.warn((String) addEntity.getBody());
//        }
        return Tips.info("修改活动商品成功");
    }

    /**
     * Description:根据ids删除活动商品
     *
     * @param ids
     * @return
     * @author hufan
     * @date 2018/11/24 16:09:12
     */
    public Tips batchDeleteByIds(String ids) {
        // 删除上架商品与新品尝鲜板块的关联
        // 根据商品id查找活动ids
        List<ActivityProduct> activityProducts = activityProductMapper.selectByIds(ids);
        if (CollectionUtils.isEmpty(activityProducts)) {
            return Tips.empty();
        }
        List<Long> productShelfIdList = activityProducts.stream().map(ActivityProduct::getProductShelfId).collect(Collectors.toList());
        // 新品尝鲜活动商品现在只关联一个活动id（现在只有一期新品尝鲜活动id）
        List<Long> activityIdList = activityProducts.stream().map(ActivityProduct::getActivityId).collect(Collectors.toList());
        Long activityId = activityIdList.get(0);
        ActivitySectionRelation relation = activitySectionRelationMapper.selectRelation(Maps.of("activityId", activityId));
        String shelfIds = StringUtils.collectionToDelimitedString(productShelfIdList, ",");
        ResponseEntity deleteEntity = baseDataServiceFeign.deleteBatch(relation.getSectionId(), shelfIds);
        if (deleteEntity.getStatusCode().isError()) {
            return Tips.warn((String) deleteEntity.getBody());
        }
        // 删除活动商品
        boolean batchDelete = activityProductMapper.deleteByIds(Arrays.asList(ids.split(","))) > 0;
        return (!batchDelete) ? Tips.warn("批量删除活动商品失败") : Tips.info("删除成功");
    }

    /**
     * 根据条件查询活动商品信息列表
     *
     * @param param
     * @return 活动商品信息列表
     */
    public Tips<Pages<ActivityProductResult>> findList(ActivityProductParam param) {
        List<ActivityProductResult> results = new ArrayList<>();
        // 查询活动商品信息
        ActivityProduct activityProduct = new ActivityProduct();
        Beans.wrap(activityProduct).any().copyOf(param);
//        BeanUtils.of(activityProduct).populate(param);
        // 根据查询条件获取上架ids
        if (Objects.nonNull(param.getBarcode()) || Objects.nonNull(param.getName())) {
            ProductShelfParam productShelfParam = new ProductShelfParam();
            productShelfParam.setName(param.getName());
            productShelfParam.setKeyword(param.getBarcode());
            ResponseEntity productShelfEntity = baseDataServiceFeign.searchProductShelves(productShelfParam);
            if (productShelfEntity.getStatusCode().isError()) {
                return Tips.warn((String) productShelfEntity.getBody());
            }
            if (Objects.isNull(productShelfEntity.getBody())) {
                return Tips.empty();
            }
            Pages<ProductShelf> productShelfPages = (Pages<ProductShelf>) productShelfEntity.getBody();
            List<ProductShelf> productShelfList = productShelfPages.getArray();
            if (!CollectionUtils.isEmpty(productShelfList)) {
                List<Long> shelfIdList = productShelfList.stream().map(ProductShelf::getId).collect(Collectors.toList());
                String shelfIds = StringUtils.collectionToDelimitedString(shelfIdList, ",");
                activityProduct.setProductShelfIds(shelfIds);
            }
        }
        // 查询活动商品信息
        List<ActivityProduct> activityProductList = activityProductMapper.pageActivityProducts(activityProduct);
        boolean pageFlag = Objects.nonNull(param.getPage()) && Objects.nonNull(param.getRows()) && param.getPage() > 0 && param.getRows() > 0;
        int total = pageFlag ? this.count(activityProduct) : activityProductList.size();

        // 查询商品上架信息
        if (!CollectionUtils.isEmpty(activityProductList)) {
            List<Long> shelfIdList = activityProductList.stream().map(ActivityProduct::getProductShelfId).collect(Collectors.toList());
            String shelfIds = StringUtils.collectionToDelimitedString(shelfIdList, ",");
            ProductShelfParam productShelfParam = new ProductShelfParam();
            productShelfParam.setIds(shelfIds);
            productShelfParam.setIncludeProduct(true);
            ResponseEntity productShelfEntity = baseDataServiceFeign.searchProductShelves(productShelfParam);
            if (productShelfEntity.getStatusCode().isError()) {
                return Tips.warn((String) productShelfEntity.getBody());
            }
            if (Objects.nonNull(productShelfEntity.getBody())){
                Pages<ProductShelf> productShelfPages = (Pages<ProductShelf>) productShelfEntity.getBody();
                List<ProductShelf> productShelfList = productShelfPages.getArray();
                // 结果转换
                // List<ActivityProduct> 转换为  List<ActivityProductResult>
                activityProductList.forEach(item -> {
                    ActivityProductResult activityProductResult = new ActivityProductResult();
                    Beans.wrap(activityProductResult).any().copyOf(item);
//                    BeanUtils.copyProperties(item, activityProductResult);
                    productShelfList.forEach(productShelf -> {
                        if (Objects.equals(activityProductResult.getProductShelfId(), productShelf.getId())){
                            Beans.wrap(activityProductResult).any().copyOf(productShelf);
//                            BeanUtils.copyProperties(productShelf,activityProductResult);
                            ProductSpecification productSpecification  = productShelf.getProductSpecification();
                            if (Objects.nonNull(productSpecification)) {
                                String specification = productSpecification.getWeight() + productSpecification.getPackagingUnit() + "*" + productSpecification.getSpecificationQty() + "份";
                                activityProductResult.setSpecification(specification);
                                activityProductResult.setProductShelfId(productShelf.getShelfId());
                                activityProductResult.setBarcode(productSpecification.getBarcode());
                                activityProductResult.setId(item.getId());
                                activityProductResult.setActivityPrice(item.getActivityPrice());
                                String specificationInfo = productShelf.getName() + " " + specification + " [" + productSpecification.getBarcode() + "]";
                                activityProductResult.setSpecificationInfo(specificationInfo);
                                results.add(activityProductResult);
                            }
                        }
                    });
                });
            }
        }


        return Tips.<Pages<ActivityProductResult>>empty().data(Pages.of(total, results));
    }

    public ActivityProduct selectActivityProduct(ActivityProduct activityProduct){
        return activityProductMapper.selectActivityProduct(activityProduct);
    }

    /**
    * Description: 查询活动商品总记录数
    *  
    * @param activityProduct
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */  
    public int count(ActivityProduct activityProduct){
        return this.activityProductMapper.pageActivityProductCounts(activityProduct);
    }

    /**
     * Description: 查询活动商品分页列表
     *
     * @param activityProduct
     * @return
     * @author yangjiawen
     * @date 2018/11/24 16:09:12
     */
    public Pages<ActivityProduct> pageList(ActivityProduct activityProduct) {
        int total = 0;
        if (activityProduct.getRows() != null && activityProduct.getRows() > 0) {
            total = this.count(activityProduct);
        }
        return Pages.of(total,
                this.activityProductMapper.pageActivityProducts(activityProduct));
    }

    public List<ActivityProduct> activityProductList(ActivityProduct activityProduct) {
        return this.activityProductMapper.pageActivityProducts(activityProduct);
    }
}

