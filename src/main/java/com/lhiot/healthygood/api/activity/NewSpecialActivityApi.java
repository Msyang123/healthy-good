package com.lhiot.healthygood.api.activity;

import com.leon.microx.util.BeanUtils;
import com.leon.microx.web.session.Sessions;
import com.lhiot.healthygood.domain.activity.*;
import com.lhiot.healthygood.feign.model.ProductShelf;
import com.lhiot.healthygood.feign.BaseDataServiceFeign;
import com.lhiot.healthygood.service.activity.ActivityProductRecordService;
import com.lhiot.healthygood.service.activity.ActivityProductService;
import com.lhiot.healthygood.service.activity.SpecialProductActivityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Api(description = "新品尝鲜活动接口")
@Slf4j
@RestController
public class NewSpecialActivityApi {

    private final SpecialProductActivityService specialProductActivityService;
    private final ActivityProductRecordService activityProductRecordService;
    private final ActivityProductService activityProductService;
    private final BaseDataServiceFeign baseDataServiceFeign;
    private Sessions session;

    @Autowired
    public NewSpecialActivityApi(ObjectProvider<Sessions> sessionsObjectProvider,
                                 SpecialProductActivityService specialProductActivityService,
                                 ActivityProductRecordService activityProductRecordService,
                                 ActivityProductService activityProductService,
                                 BaseDataServiceFeign baseDataServiceFeign) {
        this.specialProductActivityService = specialProductActivityService;
        this.activityProductRecordService = activityProductRecordService;
        this.activityProductService = activityProductService;
        this.baseDataServiceFeign = baseDataServiceFeign;
        this.session = sessionsObjectProvider.getIfAvailable();
    }

    @GetMapping("/activities/specials/products")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "page",value = "多少页",dataType = "Integer",required = true),
            @ApiImplicitParam(paramType = "query", name = "rows", value = "数据多少条", dataType = "Integer",required = true)
    })
    @ApiOperation(value = "新品尝鲜活动商品列表")
    public ResponseEntity specialActivity( HttpServletRequest request, @RequestParam Integer page, @RequestParam Integer rows){
        SpecialProductActivity specialProductActivity = specialProductActivityService.selectActivity();
        if (Objects.isNull(specialProductActivity)){
            return ResponseEntity.badRequest().body("没有开这个活动哦~");
        }
        ActivityProduct activityProduct = new ActivityProduct();
        activityProduct.setActivityId(specialProductActivity.getId());
        activityProduct.setPage(page);
        activityProduct.setRows(rows);
        List<ActivityProduct> activityProducts = activityProductService.activityProductList(activityProduct);
        if (Objects.isNull(activityProducts) || activityProducts.size() <= 0){
            return ResponseEntity.badRequest().body("活动商品是空的~");
        }
        List<ActivityProducts> activityProductsList = new ArrayList<ActivityProducts>();

        activityProducts.stream().map(item -> {
            //从基础服务里查上架商品
            ResponseEntity<ProductShelf> shelfProductResponse = baseDataServiceFeign.singleShelf(item.getProductShelfId());
            if (shelfProductResponse.getStatusCode().isError()){
                return ResponseEntity.badRequest().body(shelfProductResponse.getBody());
            }
            ProductShelf productShelf = shelfProductResponse.getBody();
            ActivityProducts product = new ActivityProducts();
            BeanUtils.copyProperties(productShelf,product);
            product.setPrice(Objects.isNull(productShelf.getPrice()) ? productShelf.getOriginalPrice() : productShelf.getPrice());
            product.setProductName(productShelf.getName());
            String sessionId = session.id(request);
            Long userId = Long.valueOf(session.user(sessionId).getUser().get("userId").toString());
            ActivityProductRecord activityProductRecord = new ActivityProductRecord();
            activityProductRecord.setUserId(userId);
            activityProductRecord.setProductShelfId(productShelf.getId());
            Integer alreadyCount = activityProductRecordService.selectRecordCount(activityProductRecord);
            product.setAlreadyBuyCount(alreadyCount);
            product.setShelfId(item.getProductShelfId());
            activityProductsList.add(product);
            return product;
        }).collect(Collectors.toList());
        NewSpecialResult newSpecialResult = new NewSpecialResult();
        BeanUtils.copyProperties(specialProductActivity,newSpecialResult);
        newSpecialResult.setActivityProductsList(activityProductsList);

        return ResponseEntity.ok(newSpecialResult);
    }
}
