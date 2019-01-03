package com.lhiot.healthygood.api.good;

import com.leon.microx.predefine.OnOff;
import com.leon.microx.util.Beans;
import com.leon.microx.util.StringUtils;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.session.Sessions;
import com.leon.microx.web.swagger.ApiParamType;
import com.lhiot.healthygood.domain.activity.ActivityProduct;
import com.lhiot.healthygood.domain.activity.ActivityProductRecord;
import com.lhiot.healthygood.domain.activity.SpecialProductActivity;
import com.lhiot.healthygood.domain.good.ProductDetailResult;
import com.lhiot.healthygood.domain.good.ProductSearchParam;
import com.lhiot.healthygood.feign.BaseDataServiceFeign;
import com.lhiot.healthygood.feign.model.*;
import com.lhiot.healthygood.feign.type.ApplicationType;
import com.lhiot.healthygood.service.activity.ActivityProductRecordService;
import com.lhiot.healthygood.service.activity.ActivityProductService;
import com.lhiot.healthygood.service.activity.SpecialProductActivityService;
import com.lhiot.healthygood.type.ShelfType;
import com.lhiot.healthygood.type.YesOrNo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
    @PostMapping("/product-sections/{id}")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = ApiParamType.PATH, name = "id", value = "板块编号", dataType = "Long", required = true),
            @ApiImplicitParam(paramType = ApiParamType.BODY, name = "productSectionParam", value = "板块信息", dataType = "ProductSectionParam")
    })
    @ApiOperation(value = "某个商品板块的商品信息列表",response = ProductSection.class)
    public ResponseEntity<Pages<ProductShelf>> productSections(@PathVariable("id") Long id, @RequestBody com.lhiot.healthygood.domain.good.ProductSectionParam productSectionParam) {
        ProductShelfParam productShelfParam = new ProductShelfParam();
        productShelfParam.setSectionId(id);
        Beans.wrap(productShelfParam).any().copyOf(productSectionParam);
//        BeanUtils.copyProperties(productSectionParam,productShelfParam);
        ResponseEntity<Pages<ProductShelf>> pagesResponseEntity = baseDataServiceFeign.searchProductShelves(productShelfParam);
        return pagesResponseEntity;
    }

    @Sessions.Uncheck
    @GetMapping("/product-sections/position")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = ApiParamType.QUERY, name = "code", value = "位置编号", dataType = "String", required = true),
            @ApiImplicitParam(paramType = ApiParamType.QUERY, name = "flag", value = "是否查询商品信息", dataType = "YesOrNo")
    })
    @ApiOperation(value = "根据位置编码查询所有商品板块列表（商品信息可选）",response = ProductSection.class,responseContainer = "List")
    public ResponseEntity positionProductSection(@RequestParam(value = "code") String code, @RequestParam(value = "flag",required = false) YesOrNo flag) {
        UiPositionParam uiPositionParam = new UiPositionParam();
        uiPositionParam.setApplicationType(ApplicationType.HEALTH_GOOD);
        uiPositionParam.setCodes(code);
        ResponseEntity<Pages<UiPosition>> uiPositionEntity = baseDataServiceFeign.searchUiPosition(uiPositionParam);
        if (Objects.isNull(uiPositionEntity) || uiPositionEntity.getStatusCode().isError()){
            return uiPositionEntity;
        }
        List<String> positionIds = new ArrayList<>();
        uiPositionEntity.getBody().getArray().forEach(uiPosition -> {
            positionIds.add(uiPosition.getId().toString());
        });

        boolean flags = false;
        if (Objects.nonNull(flag)){
            if (Objects.equals(flag.toString(),"YES")){
                flags = true;
            }
        }
        ProductSectionParam productSectionParam = new ProductSectionParam();
        productSectionParam.setPositionIds(StringUtils.collectionToDelimitedString(positionIds,","));
        productSectionParam.setIncludeShelves(flags);
        ResponseEntity<Pages<ProductSection>> pagesResponseEntity = baseDataServiceFeign.searchProductSection(productSectionParam);
        List<ProductSection> productSections = pagesResponseEntity.getBody().getArray();
        productSections.stream().filter(Objects::nonNull).forEach(productSection ->{
                if (Objects.nonNull(productSection.getProductShelfList())){
                    productSection.getProductShelfList().stream().filter(Objects::nonNull).forEach(productShelf ->{
                        productShelf.setPrice(Objects.isNull(productShelf.getPrice()) ? productShelf.getOriginalPrice() : productShelf.getPrice());
                    });
                }
            }
        );
        return pagesResponseEntity;
    }

    public void setPrice(List<ProductShelf> productShelves){
        productShelves.stream().filter(Objects::nonNull).forEach(productShelf -> {
            productShelf.setPrice(Objects.isNull(productShelf.getPrice()) ? productShelf.getOriginalPrice() : productShelf.getPrice());
        });
    }


    @GetMapping("/product/{id}")
    @ApiImplicitParam(paramType = ApiParamType.PATH, name = "id", value = "商品上架Id", dataType = "Long", required = true)
    @ApiOperation(value = "查询商品详情*",response = ProductDetailResult.class)
    public ResponseEntity singeProduct(Sessions.User user, @PathVariable(value = "id") Long id) {
        ResponseEntity<ProductShelf> productShelfResponseEntity = baseDataServiceFeign.singleShelf(id,true);
        if (Objects.isNull(productShelfResponseEntity) || productShelfResponseEntity.getStatusCode().isError()) {
            return productShelfResponseEntity;
        }
        ProductShelf productShelf = productShelfResponseEntity.getBody();
        if (Objects.isNull(productShelf)){
            return ResponseEntity.badRequest().body("没有数据");
        }
        ProductDetailResult detailResult = new ProductDetailResult();
        Beans.wrap(detailResult).any().copyOf(productShelf);
//        BeanUtils.copyProperties(productShelf,detailResult);
        //商品图片对象
        Product product = productShelf.getProductSpecification().getProduct();
        List<String> subImgs = new ArrayList<>();
        List<String> detailImgs = new ArrayList<>();
        List<String> mainImags = new ArrayList<>();
        product.getAttachments().stream().filter(Objects::nonNull).forEach(productAttachment -> {
            switch (productAttachment.getAttachmentType()){
                case SUB_IMG:
                    subImgs.add(productAttachment.getUrl());
                    break;
                case DETAIL_IMG:
                    detailImgs.add(productAttachment.getUrl());
                    break;
                case MAIN_IMG:
                    mainImags.add(productAttachment.getUrl());
                default:
                    break;
            }
        });
        detailResult.setSubImage(StringUtils.collectionToDelimitedString(subImgs,","));
        detailResult.setDetail(StringUtils.collectionToDelimitedString(detailImgs,","));

        ActivityProductRecord activityProductRecord = new ActivityProductRecord();
        SpecialProductActivity specialProductActivity = specialProductActivityService.selectActivity();
        if (Objects.nonNull(specialProductActivity)){
            Long userId = Long.valueOf(user.getUser().get("userId").toString());
            ActivityProduct activityProduct = new ActivityProduct();
            activityProduct.setProductShelfId(id);
            ActivityProduct activityProducts = activityProductService.selectActivityProduct(activityProduct);
            //如果是新品尝鲜的商品先要查出活动价格以及用户购买次数和限制购买次数
            if (Objects.nonNull(activityProducts)) {
                activityProductRecord.setUserId(userId);
                activityProductRecord.setProductShelfId(id);
                Integer alreadyCount = activityProductRecordService.selectRecordCount(activityProductRecord);
                detailResult.setActivityPrice(activityProducts.getActivityPrice());
                detailResult.setLimitCount(specialProductActivity.getLimitCount());
                detailResult.setAlreadyBuyCount(alreadyCount);
            }
        }
        return ResponseEntity.ok(detailResult);
    }

    @GetMapping("/product/cart")
    @ApiImplicitParam(paramType = ApiParamType.QUERY, name = "ids", value = "商品上架Ids", dataType = "String", required = true)
    @ApiOperation(value = "查询用户购物车商品*", response = ProductShelf.class, responseContainer = "List")
    public ResponseEntity cart(Sessions.User user, @RequestParam(value = "ids") String ids) {
        ProductShelfParam productShelfParam = new ProductShelfParam();
        productShelfParam.setIds(ids);
        productShelfParam.setApplicationType(ApplicationType.HEALTH_GOOD);
        ResponseEntity<Pages<ProductShelf>> pagesResponseEntity = baseDataServiceFeign.searchProductShelves(productShelfParam);
        if (Objects.isNull(pagesResponseEntity) || pagesResponseEntity.getStatusCode().isError()) {
            return pagesResponseEntity;
        }
        List<ProductShelf> productShelves = pagesResponseEntity.getBody().getArray();
        //新品尝鲜商品
        SpecialProductActivity specialProductActivity = specialProductActivityService.selectActivity();
        if (Objects.nonNull(specialProductActivity)) {
            ActivityProduct product = new ActivityProduct();
            product.setProductShelfIds(ids);
            product.setActivityId(specialProductActivity.getId());
            List<ActivityProduct> activityProducts = activityProductService.activityProductList(product);

            Long userId = Long.valueOf(user.getUser().get("userId").toString());
            productShelves.forEach(productShelf -> activityProducts.stream()
                    .filter(activityProduct -> Objects.equals(productShelf.getId(), activityProduct.getProductShelfId()))
                    .forEach(item -> {
                        ActivityProductRecord activityProductRecord = new ActivityProductRecord();
                        activityProductRecord.setUserId(userId);
                        activityProductRecord.setProductShelfId(item.getProductShelfId());
                        Integer alreadyCount = activityProductRecordService.selectRecordCount(activityProductRecord);
                        productShelf.setActivityPrice(item.getActivityPrice());
                        productShelf.setAlreadyBuyCount(alreadyCount);
                        productShelf.setLimitCount(specialProductActivity.getLimitCount());
                        productShelf.setPrice(Objects.isNull(productShelf.getPrice()) ? productShelf.getOriginalPrice() : productShelf.getPrice());
                    }));
        }

        return ResponseEntity.ok(productShelves);
    }

    @Sessions.Uncheck
    @ApiImplicitParam(paramType = ApiParamType.BODY, name = "productSearchParam", value = "搜索商品条件", dataType = "ProductSearchParam", required = true)
    @PostMapping("/product/search")
    @ApiOperation(value = "查询/搜索商品" )
    public ResponseEntity<Pages<ProductShelf>> searchProduct(@RequestBody ProductSearchParam productSearchParam) {
        ProductShelfParam productShelfParam = new ProductShelfParam();
        productShelfParam.setApplicationType(ApplicationType.HEALTH_GOOD);
        productShelfParam.setKeyword(productSearchParam.getKeywords());
        productShelfParam.setShelfStatus(OnOff.ON);
        productShelfParam.setShelfType(ShelfType.NORMAL);
        productShelfParam.setPage(productSearchParam.getPage());
        productShelfParam.setRows(productSearchParam.getRows());
        ResponseEntity<Pages<ProductShelf>> pagesResponseEntity = baseDataServiceFeign.searchProductShelves(productShelfParam);
        if (Objects.isNull(pagesResponseEntity) || pagesResponseEntity.getStatusCode().isError()) {
            return pagesResponseEntity;
        }
        this.setPrice(pagesResponseEntity.getBody().getArray());
        return pagesResponseEntity;
    }

    @Sessions.Uncheck
    @GetMapping("/product/recommend")
    @ApiOperation(value = "发现推荐商品列表" ,response = ProductSection.class)
    public ResponseEntity recommendProducts(){
        UiPositionParam uiPositionParam = new UiPositionParam();
        uiPositionParam.setApplicationType(ApplicationType.HEALTH_GOOD);
        uiPositionParam.setCodes("SEARCH_PRODUCTS");
        ResponseEntity<Pages<UiPosition>> uiPositionEntity = baseDataServiceFeign.searchUiPosition(uiPositionParam);
        if (Objects.isNull(uiPositionEntity) || uiPositionEntity.getStatusCode().isError()){
            return uiPositionEntity;
        }
        List<String> positionIds = new ArrayList<>();
        uiPositionEntity.getBody().getArray().forEach(uiPosition -> {
            positionIds.add(uiPosition.getId().toString());
        });
        ProductSectionParam productSectionParam = new ProductSectionParam();
        productSectionParam.setPositionIds(StringUtils.collectionToDelimitedString(positionIds,","));
        productSectionParam.setIncludeShelves(true);
        ResponseEntity<Pages<ProductSection>> pagesResponseEntity = baseDataServiceFeign.searchProductSection(productSectionParam);
        return ResponseEntity.ok(pagesResponseEntity.getBody().getArray().get(0));
    }

}
