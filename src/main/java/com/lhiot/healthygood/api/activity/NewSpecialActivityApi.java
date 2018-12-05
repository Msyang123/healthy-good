package com.lhiot.healthygood.api.activity;

import com.leon.microx.util.BeanUtils;
import com.leon.microx.web.result.Tips;
import com.leon.microx.web.session.Sessions;
import com.leon.microx.web.swagger.ApiParamType;
import com.lhiot.healthygood.domain.activity.*;
import com.lhiot.healthygood.domain.good.PagesParam;
import com.lhiot.healthygood.domain.good.ProductSearchParam;
import com.lhiot.healthygood.feign.model.ProductShelf;
import com.lhiot.healthygood.feign.BaseDataServiceFeign;
import com.lhiot.healthygood.service.activity.ActivityProductRecordService;
import com.lhiot.healthygood.service.activity.ActivityProductService;
import com.lhiot.healthygood.service.activity.SpecialProductActivityService;
import com.lhiot.healthygood.util.FeginResponseTools;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/activities/specials/products")
    @ApiImplicitParam(paramType = ApiParamType.BODY, name = "pagesParam", value = "分页对象", dataType = "PagesParam",required = true)
    @ApiOperation(value = "新品尝鲜活动商品列表")
    public ResponseEntity specialActivity(Sessions.User user, @RequestBody PagesParam pagesParam){
        SpecialProductActivity specialProductActivity = specialProductActivityService.selectActivity();
        if (Objects.isNull(specialProductActivity)){
            return ResponseEntity.badRequest().body("没有开这个活动哦~");
        }
        ActivityProduct activityProduct = new ActivityProduct();
        activityProduct.setActivityId(specialProductActivity.getId());
        BeanUtils.copyProperties(pagesParam,activityProduct);
        List<ActivityProduct> activityProductsList = activityProductService.activityProductList(activityProduct);
        if (Objects.isNull(activityProductsList) || activityProductsList.size() <= 0){
            return ResponseEntity.badRequest().body("活动商品是空的~");
        }
        List<ActivityProducts> activityProducts = new ArrayList<ActivityProducts>();

        activityProductsList.forEach(item -> {
            //从基础服务里查上架商品
            ResponseEntity<ProductShelf> shelfProductResponse = baseDataServiceFeign.singleShelf(item.getProductShelfId());
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
        newSpecialResult.setActivityProductsList(activityProducts);

        return ResponseEntity.ok(newSpecialResult);
    }
}
