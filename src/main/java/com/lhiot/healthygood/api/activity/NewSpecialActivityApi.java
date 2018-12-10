package com.lhiot.healthygood.api.activity;

import com.leon.microx.util.BeanUtils;
import com.leon.microx.util.Maps;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.leon.microx.web.session.Sessions;
import com.leon.microx.web.swagger.ApiParamType;
import com.lhiot.healthygood.domain.activity.*;
import com.lhiot.healthygood.domain.activity.model.ActivityProductParam;
import com.lhiot.healthygood.domain.activity.model.ActivityProductResult;
import com.lhiot.healthygood.domain.good.PagesParam;
import com.lhiot.healthygood.feign.BaseDataServiceFeign;
import com.lhiot.healthygood.feign.model.ProductShelf;
import com.lhiot.healthygood.service.activity.ActivityProductRecordService;
import com.lhiot.healthygood.service.activity.ActivityProductService;
import com.lhiot.healthygood.service.activity.SpecialProductActivityService;
import com.lhiot.healthygood.util.FeginResponseTools;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Api(description = "新品尝鲜活动接口")
@Slf4j
@RestController
public class NewSpecialActivityApi {

    private final SpecialProductActivityService specialProductActivityService;
    private final ActivityProductRecordService activityProductRecordService;
    private final ActivityProductService activityProductService;
    private final BaseDataServiceFeign baseDataServiceFeign;

    @Autowired
    public NewSpecialActivityApi(
                                 SpecialProductActivityService specialProductActivityService,
                                 ActivityProductRecordService activityProductRecordService,
                                 ActivityProductService activityProductService,
                                 BaseDataServiceFeign baseDataServiceFeign) {
        this.specialProductActivityService = specialProductActivityService;
        this.activityProductRecordService = activityProductRecordService;
        this.activityProductService = activityProductService;
        this.baseDataServiceFeign = baseDataServiceFeign;
    }

    @PostMapping("/activities/specials/products")
    @ApiImplicitParam(paramType = ApiParamType.BODY, name = "pagesParam", value = "分页对象", dataType = "PagesParam",required = true)
    @ApiOperation(value = "新品尝鲜活动商品列表",response = NewSpecialResult.class)
    public ResponseEntity<Tips> specialActivity(Sessions.User user, @RequestBody PagesParam pagesParam){
        SpecialProductActivity specialProductActivity = specialProductActivityService.selectActivity();
        if (Objects.isNull(specialProductActivity)){
            return ResponseEntity.badRequest().body(Tips.warn("没有开这个活动哦"));
        }
        ActivityProduct activityProduct = new ActivityProduct();
        activityProduct.setActivityId(specialProductActivity.getId());
        BeanUtils.copyProperties(pagesParam,activityProduct);
        List<ActivityProduct> activityProductsList = activityProductService.activityProductList(activityProduct);
        if (Objects.isNull(activityProductsList) || activityProductsList.size() <= 0){
            return ResponseEntity.badRequest().body(Tips.warn("活动商品是空的~"));
        }
        List<ActivityProducts> activityProducts = new ArrayList<ActivityProducts>();

        activityProductsList.forEach(item -> {
            //从基础服务里查上架商品
            ResponseEntity<ProductShelf> shelfProductResponse = baseDataServiceFeign.singleShelf(item.getProductShelfId(),false);
            Tips<ProductShelf> tips = FeginResponseTools.convertResponse(shelfProductResponse);
            if (tips.err()){
                return ;
            }
            ProductShelf productShelf = shelfProductResponse.getBody();
            if (Objects.isNull(productShelf)){
                return;
            }
            ActivityProducts product = new ActivityProducts();
            BeanUtils.copyProperties(productShelf,product);
            product.setPrice(Objects.isNull(productShelf.getPrice()) ? productShelf.getOriginalPrice() : productShelf.getPrice());
            product.setProductName(productShelf.getName());

            Long userId = Long.valueOf(user.getUser().get("userId").toString());
            ActivityProductRecord activityProductRecord = new ActivityProductRecord();
            activityProductRecord.setUserId(userId);
            activityProductRecord.setProductShelfId(productShelf.getId());
            Integer alreadyCount = activityProductRecordService.selectRecordCount(activityProductRecord);
            product.setAlreadyBuyCount(alreadyCount);
            product.setShelfId(item.getProductShelfId());
            activityProducts.add(product);
        });
        NewSpecialResult newSpecialResult = new NewSpecialResult();
        BeanUtils.copyProperties(specialProductActivity,newSpecialResult);
        newSpecialResult.setActivityProductList(activityProducts);
        Tips tips = new Tips();
        tips.setData(newSpecialResult);
        return ResponseEntity.ok(tips);
    }


    @Sessions.Uncheck
    @ApiOperation("添加新品尝鲜活动商品(后台)")
    @ApiImplicitParam(paramType = ApiParamType.BODY, name = "activityProduct", value = "新品尝鲜活动商品", dataType = "ActivityProduct", required = true)
    @PostMapping("/activity-products")
    public ResponseEntity create(@Valid @RequestBody ActivityProduct activityProduct) {
        log.debug("添加新品尝鲜活动商品\t param:{}", activityProduct);

        Tips tips = activityProductService.create(activityProduct);
        if (tips.err()) {
            return ResponseEntity.badRequest().body(Tips.warn(tips.getMessage()));
        }
        Long activityProductId = Long.valueOf(tips.getMessage());
        return activityProductId > 0 ?
                ResponseEntity.created(URI.create("/activity-products/" + activityProductId)).body(Maps.of("id", activityProductId)) :
                ResponseEntity.badRequest().body(Tips.warn("添加新品尝鲜活动商品失败!"));
    }

    @Sessions.Uncheck
    @ApiOperation("修改新品尝鲜活动商品(后台)")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = ApiParamType.PATH, name = "id", value = "新品尝鲜活动商品id", dataType = "Long", required = true),
            @ApiImplicitParam(paramType = ApiParamType.BODY, name = "activityProduct", value = "新品尝鲜活动商品", dataType = "ActivityProduct", required = true)
    })
    @PutMapping("/activity-products/{id}")
    public ResponseEntity update(@PathVariable("id") Long id, @Valid @RequestBody ActivityProduct activityProduct) {
        log.debug("修改新品尝鲜活动商品\t param:{}", activityProduct);

        Tips tips = activityProductService.updateById(id,activityProduct);
        return !tips.err() ? ResponseEntity.ok().build() : ResponseEntity.badRequest().body(Tips.warn("修改信息失败!"));
    }

    @Sessions.Uncheck
    @ApiOperation("根据ids删除新品尝鲜活动商品(后台)")
    @ApiImplicitParam(paramType = ApiParamType.PATH, name = "ids", value = "多个新品尝鲜活动商品id以英文逗号分隔", dataType = "String", required = true)
    @DeleteMapping("/activity-products/{ids}")
    public ResponseEntity batchDelete(@PathVariable("ids") String ids) {
        log.debug("批量删除新品尝鲜活动商品\t param:{}", ids);

        Tips tips = activityProductService.batchDeleteByIds(ids);
        return !tips.err() ? ResponseEntity.noContent().build() : ResponseEntity.badRequest().body(Tips.warn(tips.getMessage()));
    }

    @Sessions.Uncheck
    @ApiOperation(value = "根据条件分页查询新品尝鲜活动商品信息列表(后台)", response = ActivityProductResult.class, responseContainer = "Set")
    @ApiImplicitParam(paramType = ApiParamType.BODY, name = "param", value = "查询条件", dataType = "ActivityProductParam")
    @PostMapping("/activity-products/pages")
    public ResponseEntity<Tips> search(@RequestBody ActivityProductParam param) {
        log.debug("根据条件分页查询新品尝鲜活动商品信息列表\t param:{}", param);

        Pages<ActivityProductResult> pages = activityProductService.findList(param);
        Tips tips = new Tips();
        tips.setData(pages);
        return ResponseEntity.ok(tips);
    }
}
