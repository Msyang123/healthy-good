package com.lhiot.healthygood.api.activity;

import com.leon.microx.predefine.OnOff;
import com.leon.microx.util.Beans;
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
import com.lhiot.healthygood.feign.model.ProductShelfParam;
import com.lhiot.healthygood.feign.type.ApplicationType;
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
    @ApiOperation(value = "新品尝鲜活动商品列表*",response = NewSpecialResult.class)
    public ResponseEntity specialActivity(Sessions.User user, @RequestBody PagesParam pagesParam){
        SpecialProductActivity specialProductActivity = specialProductActivityService.selectActivity();
        if (Objects.isNull(specialProductActivity)){
            return ResponseEntity.badRequest().body("没有开这个活动");
        }
        ActivityProduct activityProduct = new ActivityProduct();
        activityProduct.setActivityId(specialProductActivity.getId());
        Beans.from(pagesParam).to(activityProduct);
        List<ActivityProduct> activityProductsList = activityProductService.activityProductList(activityProduct);

        ProductShelfParam productShelfParam = new ProductShelfParam();
        productShelfParam.setApplicationType(ApplicationType.HEALTH_GOOD);
        productShelfParam.setShelfStatus(OnOff.ON);
        ResponseEntity<Pages<ProductShelf>> pagesResponseEntity = baseDataServiceFeign.searchProductShelves(productShelfParam);
        if (Objects.isNull(pagesResponseEntity) || pagesResponseEntity.getStatusCode().isError()) {
            return pagesResponseEntity;
        }
        List<ProductShelf> productShelves = pagesResponseEntity.getBody().getArray();
        List<ActivityProducts> activityProducts = new ArrayList<ActivityProducts>();
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        productShelves.forEach(productShelf -> activityProductsList.stream()
                .filter(s -> Objects.equals(productShelf.getId(), s.getProductShelfId()))
                .forEach(item -> {
                    ActivityProductRecord activityProductRecord = new ActivityProductRecord();
                    activityProductRecord.setUserId(userId);
                    activityProductRecord.setProductShelfId(item.getProductShelfId());
                    Integer alreadyCount = activityProductRecordService.selectRecordCount(activityProductRecord);
                    ActivityProducts product = new ActivityProducts();
                    //BeanUtils.copyProperties(productShelf,product);
                    Beans.from(productShelf).to(product);
                    product.setPrice(Objects.isNull(productShelf.getPrice()) ? productShelf.getOriginalPrice() : productShelf.getPrice());
                    product.setProductName(productShelf.getName());
                    product.setAlreadyBuyCount(alreadyCount);
                    product.setShelfId(item.getProductShelfId());
                    product.setActivityPrice(item.getActivityPrice());
                    activityProducts.add(product);
                }));
        NewSpecialResult newSpecialResult = new NewSpecialResult();
        Beans.from(specialProductActivity).to(newSpecialResult);
        newSpecialResult.setActivityProductList(activityProducts);
        return ResponseEntity.ok(newSpecialResult);
    }


    @Sessions.Uncheck
    @ApiOperation("添加新品尝鲜活动商品(后台)")
    @ApiImplicitParam(paramType = ApiParamType.BODY, name = "activityProduct", value = "新品尝鲜活动商品", dataType = "ActivityProduct", required = true)
    @PostMapping("/activity-products")
    public ResponseEntity create(@Valid @RequestBody ActivityProduct activityProduct) {
        log.debug("添加新品尝鲜活动商品\t param:{}", activityProduct);

        Tips tips = activityProductService.create(activityProduct);
        if (tips.err()) {
            return ResponseEntity.badRequest().body(tips.getMessage());
        }
        Long activityProductId = Long.valueOf(tips.getMessage());
        return activityProductId > 0
                ? ResponseEntity.created(URI.create("/activity-products/" + activityProductId)).body(Maps.of("id", activityProductId))
                : ResponseEntity.badRequest().body("添加新品尝鲜活动商品失败!");
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
        return tips.err() ? ResponseEntity.badRequest().body("修改新品尝鲜活动商品失败!") : ResponseEntity.ok().build();
    }

    @Sessions.Uncheck
    @ApiOperation("根据ids删除新品尝鲜活动商品(后台)")
    @ApiImplicitParam(paramType = ApiParamType.PATH, name = "ids", value = "多个新品尝鲜活动商品id以英文逗号分隔", dataType = "String", required = true)
    @DeleteMapping("/activity-products/{ids}")
    public ResponseEntity batchDelete(@PathVariable("ids") String ids) {
        log.debug("批量删除新品尝鲜活动商品\t param:{}", ids);

        Tips tips = activityProductService.batchDeleteByIds(ids);
        return tips.err() ? ResponseEntity.badRequest().body(tips.getMessage()) : ResponseEntity.noContent().build();
    }

    @Sessions.Uncheck
    @ApiOperation(value = "根据条件分页查询新品尝鲜活动商品信息列表(后台)", response = ActivityProductResult.class, responseContainer = "Set")
    @ApiImplicitParam(paramType = ApiParamType.BODY, name = "param", value = "查询条件", dataType = "ActivityProductParam")
    @PostMapping("/activity-products/pages")
    public ResponseEntity search(@RequestBody ActivityProductParam param) {
        log.debug("根据条件分页查询新品尝鲜活动商品信息列表\t param:{}", param);

        Tips tips = activityProductService.findList(param);
        return tips.err() ? ResponseEntity.badRequest().body(tips.getMessage()) : ResponseEntity.ok(tips.getData());
    }
}
