package com.lhiot.healthygood.service.activity;

import com.google.common.base.Joiner;
import com.leon.microx.util.BeanUtils;
import com.leon.microx.util.Maps;
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
import com.lhiot.healthygood.mapper.activity.ActivityProductMapper;
import com.lhiot.healthygood.mapper.activity.ActivitySectionRelationMapper;
import com.lhiot.healthygood.type.ActivityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Description:活动商品服务类
 *
 * @author yangjiawen
 * @date 2018/11/24
 */
@Service
@Transactional
@Component
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
            return Tips.warn("上架id重复关联，添加失败");
        }
        // 添加活动商品
        boolean addActivityProduct = activityProductMapper.create(activityProduct) > 0;
        if (!addActivityProduct) {
            Tips.warn("添加活动商品失败！");
        }
        // 添加上架商品与新品尝鲜板块的关联
        // 根据活动id查询关联的板块id
        @NotNull(message = "活动id不为空") Long activityId = activityProduct.getActivityId();
        // 一个定制板块活动只会关联一个商品板块,如果有多个活动则要修改
        ActivitySectionRelation relation = activitySectionRelationMapper.selectRelation(Maps.of("activityId", activityId));
        // 根据板块id和上架id在基础服务中添加关联关系
        Long sectionId = relation.getSectionId();
        ProductSectionRelation productSectionRelation = new ProductSectionRelation();
        productSectionRelation.setSectionId(sectionId);
        productSectionRelation.setShelfId(productShelfId);
        ResponseEntity entity = baseDataServiceFeign.create(productSectionRelation);
        if (entity.getStatusCode().isError()) {
            return Tips.warn(entity.getBody().toString());
        }
        return Tips.info(activityProduct.getId() + "");
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
            Tips.warn("修改活动商品失败");
        }
        // 修改上架商品与新品尝鲜板块的关联
        // 先删除 再新增
        @NotNull(message = "活动id不为空") Long activityId = activityProduct.getActivityId();
        // 一个定制板块活动只会关联一个商品板块,如果有多个活动则要修改
        ActivitySectionRelation relation = activitySectionRelationMapper.selectRelation(Maps.of("activityId", activityId));
        Long sectionId = relation.getSectionId();
        Long shelfId = activityProduct.getProductShelfId();
        ResponseEntity deleteEntity = baseDataServiceFeign.deleteBatch(sectionId, beforeShelfId.toString());
        if (deleteEntity.getStatusCode().isError()) {
            return Tips.warn(deleteEntity.getBody().toString());
        }
        ProductSectionRelation productSectionRelation = new ProductSectionRelation();
        productSectionRelation.setSectionId(sectionId);
        productSectionRelation.setShelfId(shelfId);
        ResponseEntity addEntity = baseDataServiceFeign.create(productSectionRelation);
        if (addEntity.getStatusCode().isError()) {
            return Tips.warn(addEntity.getBody().toString());
        }
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
        List<Long> productShelfIdList = activityProducts.stream().map(ActivityProduct::getProductShelfId).collect(Collectors.toList());
        // 新品尝鲜活动商品现在只关联一个活动id（现在只有一期新品尝鲜活动id）
        List<Long> activityIdList = activityProducts.stream().map(ActivityProduct::getActivityId).collect(Collectors.toList());
        Long activityId = activityIdList.get(0);
        ActivitySectionRelation relation = activitySectionRelationMapper.selectRelation(Maps.of("activityId", activityId));
        String shelfIds = Joiner.on(",").join(productShelfIdList);
        ResponseEntity deleteEntity = baseDataServiceFeign.deleteBatch(relation.getSectionId(), shelfIds);
        if (deleteEntity.getStatusCode().isError()) {
            Tips.warn(deleteEntity.getBody().toString());
        }

        // 删除活动商品
        boolean batchDelete = activityProductMapper.deleteByIds(Arrays.asList(ids.split(","))) > 0;
        if (!batchDelete) {
            Tips.warn("批量删除活动商品失败");
        }
        return Tips.info("删除成功");
    }

    /**
     * 根据条件查询活动商品信息列表
     *
     * @param param
     * @return 活动商品信息列表
     */
    public Pages<ActivityProductResult> findList(ActivityProductParam param) {
        // 查询活动商品信息
        ActivityProduct activityProduct = new ActivityProduct();
        BeanUtils.copyProperties(param, activityProduct);
        List<ActivityProduct> activityProductList = activityProductMapper.pageActivityProducts(activityProduct);
        boolean pageFlag = Objects.nonNull(param.getPage()) && Objects.nonNull(param.getRows()) && param.getPage() > 0 && param.getRows() > 0;
        int total = pageFlag ? this.count(activityProduct) : activityProductList.size();

        // 查询商品上架信息
        List<Long> shelfIdList = activityProductList.stream().map(ActivityProduct::getProductShelfId).collect(Collectors.toList());
        String shelfIds = Joiner.on(",").join(shelfIdList);
        ProductShelfParam productShelfParam = new ProductShelfParam();
        productShelfParam.setIds(shelfIds);
        ResponseEntity productShelfEntity = baseDataServiceFeign.searchProductShelves(productShelfParam);
        if (productShelfEntity.getStatusCode().isError()) {
            Tips.warn(productShelfEntity.getBody().toString());
        }
        Pages<ProductShelf> productShelfPages = (Pages<ProductShelf>) productShelfEntity.getBody();
        List<ProductShelf> productShelfList = productShelfPages.getArray();

        // 结果转换
        List<ActivityProductResult> results = new ArrayList<>();
        // List<ActivityProduct> 转换为  List<ActivityProductResult>
        activityProductList.forEach(item -> {
            ActivityProductResult activityProductResult = new ActivityProductResult();
            BeanUtils.copyProperties(item, activityProductResult);
            ProductShelf productShelf = productShelfList.get(activityProductList.indexOf(item));
            BeanUtils.copyProperties(productShelf, activityProductResult);
            String specification = productShelf.getProductSpecification().getWeight() + productShelf.getProductSpecification().getPackagingUnit() + "*" + productShelf.getProductSpecification().getSpecificationQty() + "份";
            activityProductResult.setSpecification(specification);
            activityProductResult.setBarcode(productShelf.getProductSpecification().getBarcode());
            results.add(activityProductResult);
        });
        return Pages.of(total, results);
    }

    /**
     * Description: 查询活动商品总记录数
     *
     * @param activityProduct
     * @return
     * @author yangjiawen
     * @date 2018/11/24 16:09:12
     */
    public int count(ActivityProduct activityProduct) {
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

