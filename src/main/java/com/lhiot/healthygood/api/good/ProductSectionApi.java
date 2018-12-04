package com.lhiot.healthygood.api.good;

import com.leon.microx.predefine.OnOff;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.leon.microx.web.session.Sessions;
import com.leon.microx.web.swagger.ApiParamType;
import com.lhiot.healthygood.domain.activity.ActivityProduct;
import com.lhiot.healthygood.domain.activity.ActivityProductRecord;
import com.lhiot.healthygood.domain.activity.SpecialProductActivity;
import com.lhiot.healthygood.feign.BaseDataServiceFeign;
import com.lhiot.healthygood.feign.model.ProductSection;
import com.lhiot.healthygood.feign.model.ProductSectionParam;
import com.lhiot.healthygood.feign.model.ProductShelf;
import com.lhiot.healthygood.feign.model.ProductShelfParam;
import com.lhiot.healthygood.feign.type.ApplicationType;
import com.lhiot.healthygood.service.activity.ActivityProductRecordService;
import com.lhiot.healthygood.service.activity.ActivityProductService;
import com.lhiot.healthygood.service.activity.SpecialProductActivityService;
import com.lhiot.healthygood.type.ShelfType;
import com.lhiot.healthygood.util.FeginResponseTools;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Api(description = "商品板块类接口")
@Slf4j
@RestController
public class ProductSectionApi {

    private final BaseDataServiceFeign baseDataServiceFeign;
    private final SpecialProductActivityService specialProductActivityService;
    private final ActivityProductRecordService activityProductRecordService;
    private final ActivityProductService activityProductService;

    @Autowired
    public ProductSectionApi(BaseDataServiceFeign baseDataServiceFeign, SpecialProductActivityService specialProductActivityService, ActivityProductRecordService activityProductRecordService, ActivityProductService activityProductService) {
        this.baseDataServiceFeign = baseDataServiceFeign;
        this.specialProductActivityService = specialProductActivityService;
        this.activityProductRecordService = activityProductRecordService;
        this.activityProductService = activityProductService;
    }

    @Sessions.Uncheck
    @GetMapping("/product-sections/{id}")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = ApiParamType.PATH,name = "id",value = "板块编号",dataType = "Long",required = true),
            @ApiImplicitParam(paramType = ApiParamType.QUERY, name = "flag", value = "是否查询商品信息", dataType = "Boolean")
    })
    @ApiOperation(value = "某个商品板块的商品信息列表", response = ProductSection.class)
    public ResponseEntity productSections(@PathVariable("id") Long id, @RequestParam(value = "flag", required = false) boolean flag){
        ResponseEntity<ProductSection> productSectionResponseEntity = baseDataServiceFeign.singleProductSection(id,flag,null);
        Tips tips = FeginResponseTools.convertResponse(productSectionResponseEntity);
        if (tips.err()){
            return ResponseEntity.badRequest().body(tips);
        }
        return ResponseEntity.badRequest().body(productSectionResponseEntity.getBody());
    }

    @Sessions.Uncheck
    @PostMapping("/product-sections/position")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = ApiParamType.QUERY,name = "id",value = "位置编号",dataType = "Long",required = true),
            @ApiImplicitParam(paramType = ApiParamType.QUERY, name = "flag", value = "是否查询商品信息", dataType = "Boolean")
    })
    @ApiOperation(value = "根据位置编码查询所有商品板块列表（商品信息可选）", response = ProductSection.class, responseContainer = "Set")
    public ResponseEntity positionProductSection(@RequestParam(value = "positionId") Long id,@RequestParam(value = "flag", required = false) boolean flag){
        ProductSectionParam productSectionParam = new ProductSectionParam();
        productSectionParam.setPositionId(id);
        productSectionParam.setIncludeShelves(flag);
        ResponseEntity<Pages<ProductSection>> pagesResponseEntity = baseDataServiceFeign.searchProductSection(productSectionParam);
        Tips tips = FeginResponseTools.convertResponse(pagesResponseEntity);
        if (tips.err()){
            return ResponseEntity.badRequest().body(tips);
        }
        return ResponseEntity.badRequest().body(pagesResponseEntity.getBody());
    }


    @GetMapping("/product/{id}")
    @ApiImplicitParam(paramType = ApiParamType.PATH,name = "id",value = "商品上架Id",dataType = "Long",required = true)
    @ApiOperation(value = "查询商品详情", response = ProductShelf.class, responseContainer = "Set")
    public ResponseEntity singeProduct(Sessions.User user,@PathVariable(value = "id") Long id){
        ResponseEntity<ProductShelf> productShelfResponseEntity =  baseDataServiceFeign.singleShelf(id);
        Tips tips = FeginResponseTools.convertResponse(productShelfResponseEntity);
        if (tips.err()){
            return ResponseEntity.badRequest().body(tips);
        }
        ProductShelf productShelf = productShelfResponseEntity.getBody();
        ActivityProductRecord activityProductRecord = new ActivityProductRecord();
        SpecialProductActivity specialProductActivity = specialProductActivityService.selectActivity();
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        ActivityProduct activityProduct = new ActivityProduct();
        activityProduct.setProductShelfId(id);
        ActivityProduct activityProducts = activityProductService.selectActivityProduct(activityProduct);
        //如果是新品尝鲜的商品先要查出活动价格以及用户购买次数和限制购买次数
        if (Objects.nonNull(specialProductActivity) && Objects.nonNull(activityProducts)){
            activityProductRecord.setUserId(userId);
            activityProductRecord.setProductShelfId(id);
            Integer alreadyCount = activityProductRecordService.selectRecordCount(activityProductRecord);
            productShelf.setActivityPrice(activityProducts.getActivityPrice());
            productShelf.setLimitCount(specialProductActivity.getLimitCount());
            productShelf.setAlreadyBuyAmount(alreadyCount);
        }
        return ResponseEntity.ok(productShelf);
    }

    @GetMapping("/product/cart")
    @ApiImplicitParam(paramType = ApiParamType.QUERY,name = "ids",value = "商品上架Ids",dataType = "String",required = true)
    @ApiOperation(value = "查询用户购物车商品", response = ProductShelf.class, responseContainer = "List")
    public ResponseEntity cart(Sessions.User user,@RequestParam(value = "ids") String ids){
        ProductShelfParam productShelfParam = new ProductShelfParam();
        productShelfParam.setIds(ids);
        productShelfParam.setApplicationType("FRUIT_DOCTOR");
        ResponseEntity<Pages<ProductShelf>> pagesResponseEntity = baseDataServiceFeign.searchProductShelves(productShelfParam);
        Tips tips = FeginResponseTools.convertResponse(pagesResponseEntity);
        if (tips.err()){
            return ResponseEntity.badRequest().body(tips);
        }
        List<ProductShelf> productShelves = pagesResponseEntity.getBody().getArray();
        //新品尝鲜商品
        SpecialProductActivity specialProductActivity = specialProductActivityService.selectActivity();
        if (Objects.nonNull(specialProductActivity)){
            ActivityProduct product = new ActivityProduct();
            product.setProductShelfIds(ids);
            product.setActivityId(specialProductActivity.getId());
            List<ActivityProduct> activityProducts = activityProductService.activityProductList(product);

            Long userId = Long.valueOf(user.getUser().get("userId").toString());
            productShelves.forEach(productShelf -> activityProducts.stream()
                    .filter(activityProduct -> Objects.equals(productShelf.getId(),activityProduct.getProductShelfId()))
                    .forEach(item -> {
                        ActivityProductRecord activityProductRecord = new ActivityProductRecord();
                        activityProductRecord.setUserId(userId);
                        activityProductRecord.setProductShelfId(item.getProductShelfId());
                        Integer alreadyCount = activityProductRecordService.selectRecordCount(activityProductRecord);
                        productShelf.setActivityPrice(item.getActivityPrice());
                        productShelf.setAlreadyBuyAmount(alreadyCount);
                        productShelf.setLimitCount(specialProductActivity.getLimitCount());
                    }));
        }

        return ResponseEntity.ok(productShelves);
    }

    /*@ApiImplicitParams({
            @ApiImplicitParam(paramType = ApiParamType.QUERY,name = "keywords", value = "搜索关键词",dataType = "String",required = true),
            @ApiImplicitParam(paramType = ApiParamType.QUERY,name = "page",value = "多少页",dataType = "Integer",required = true),
            @ApiImplicitParam(paramType = ApiParamType.QUERY, name = "rows", value = "数据多少条", dataType = "Integer",required = true)
    })
    public ResponseEntity searchProduct(@PathVariable(value = "keywords") String keywords, @RequestParam Integer page, @RequestParam Integer rows){
        ProductShelfParam productShelfParam = new ProductShelfParam();
        productShelfParam.setApplicationType("FRUIT_DOCTOR");
        productShelfParam.setKeyword(keywords);
        productShelfParam.setShelfStatus(OnOff.ON);
        productShelfParam.setShelfType(ShelfType.NORMAL);
        productShelfParam.setPage(page);
        productShelfParam.setRows(rows);
        ResponseEntity<Pages<ProductShelf>> pagesResponseEntity = baseDataServiceFeign.searchProductShelves(productShelfParam);
        Tips tips = FeginResponseTools.convertResponse(pagesResponseEntity);
        if (tips.err()){
            return ResponseEntity.badRequest().body(tips);
        }
    }*/


}