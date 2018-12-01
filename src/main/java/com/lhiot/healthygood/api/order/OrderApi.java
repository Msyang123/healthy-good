package com.lhiot.healthygood.api.order;

import com.leon.microx.predefine.OnOff;
import com.leon.microx.util.BeanUtils;
import com.leon.microx.util.Calculator;
import com.leon.microx.util.StringUtils;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.leon.microx.web.session.Sessions;
import com.lhiot.healthygood.feign.BaseDataServiceFeign;
import com.lhiot.healthygood.feign.OrderServiceFeign;
import com.lhiot.healthygood.feign.ThirdpartyServerFeign;
import com.lhiot.healthygood.feign.model.*;
import com.lhiot.healthygood.feign.type.ApplicationType;
import com.lhiot.healthygood.feign.type.OrderStatus;
import com.lhiot.healthygood.util.FeginResponseTools;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Api(description = "普通订单接口")
@Slf4j
@RestController
public class OrderApi {
    private final BaseDataServiceFeign baseDataServiceFeign;
    private final ThirdpartyServerFeign thirdpartyServerFeign;
    private final OrderServiceFeign orderServiceFeign;

    @Autowired
    public OrderApi(BaseDataServiceFeign baseDataServiceFeign, ThirdpartyServerFeign thirdpartyServerFeign, OrderServiceFeign orderServiceFeign) {
        this.baseDataServiceFeign = baseDataServiceFeign;
        this.thirdpartyServerFeign = thirdpartyServerFeign;
        this.orderServiceFeign = orderServiceFeign;
    }

    @PostMapping("/orders")
    @ApiOperation("和色果膳--创建订单")
    @ApiImplicitParam(paramType = "body", name = "orderParam", dataType = "CreateOrderParam", required = true, value = "创建订单传入参数")
    public ResponseEntity<Tips> createOrder(@Valid @RequestBody CreateOrderParam orderParam, Sessions.User user) {

        Map<String, Object> sessionUserMap = user.getUser();
        Long userId = Long.valueOf(sessionUserMap.get("userId").toString());
        orderParam.setUserId(userId);//设置业务用户id
        orderParam.setApplicationType(ApplicationType.FRUIT_DOCTOR);

        String storeCode = orderParam.getOrderStore().getStoreCode();
        //判断门店是否存在
        ResponseEntity<Store> storeResponseEntity = baseDataServiceFeign.findStoreByCode(storeCode);
        Tips<Store> storeTips = FeginResponseTools.convertResponse(storeResponseEntity);
        if (storeTips.err()) {
            return ResponseEntity.badRequest().body(storeTips);
        }
        Store store = storeTips.getData();

        //给订单门店赋值
        OrderStore orderStore = new OrderStore();
        orderStore.setStoreId(store.getId());
        orderStore.setStoreCode(store.getCode());
        orderStore.setStoreName(store.getName());
        orderParam.setOrderStore(orderStore);

        List<OrderProduct> orderProducts = orderParam.getOrderProducts();
        //依据上架ids查询上架商品信息
        String[] shelfIds = orderProducts.parallelStream().map(OrderProduct::getShelfId).toArray(String[]::new);

        ProductShelfParam productShelfParam = new ProductShelfParam();
        productShelfParam.setIds(StringUtils.join(",", shelfIds));
        productShelfParam.setShelfStatus(OnOff.ON);
        //查找基础服务上架商品信息
        Tips<Pages<ProductShelf>> productShelfTips = FeginResponseTools.convertResponse(baseDataServiceFeign.searchProductShelves(productShelfParam));
        if (productShelfTips.err()) {
            return ResponseEntity.badRequest().body(productShelfTips);
        }
        Pages<ProductShelf> productShelfPages = productShelfTips.getData();
        String[] barcodes = productShelfPages.getArray().parallelStream()
                .map(ProductShelf::getProductSpecification)
                .map(ProductSpecification::getBarcode).toArray(String[]::new);

        //查询门店h4库存信息
        Tips<Map<String, Object>> quserSkuTips = FeginResponseTools.convertResponse(thirdpartyServerFeign.querySku(orderParam.getOrderStore().getStoreCode(), barcodes));

        if (quserSkuTips.err()) {
            return ResponseEntity.badRequest().body(quserSkuTips);
        }
        List<Map> businvs = (List<Map>) quserSkuTips.getData().get("businvs");
        //校验库存是否足够
        for (ProductShelf productShelf : productShelfPages.getArray()) {
            for (Map businv : businvs) {
                if (Objects.equals(productShelf.getProductSpecification().getBarcode(), businv.get("barCode"))
                        && productShelf.getProductSpecification().getLimitInventory() > Double.valueOf(businv.get("qty").toString())) {
                    return ResponseEntity.badRequest().body(
                            Tips.of(HttpStatus.BAD_REQUEST, String.format("%s实际库存%s低于安全库存%d，不允许销售", productShelf.getProductSpecification().getBarcode(), businv.get("qty").toString(), productShelf.getProductSpecification().getLimitInventory())));
                }
            }
        }

        //给商品赋值规格数量
        orderParam.getOrderProducts().forEach(orderProduct -> productShelfPages.getArray().stream()
            //上架id相同的订单商品信息，通过基础服务获取的赋值给订单商品信息
            .filter(productShelf -> Objects.equals(orderProduct.getShelfId(), productShelf.getId()))
            .forEach(item -> {
                BeanUtils.copyProperties(item, orderProduct);
                //设置规格信息
                ProductSpecification productSpecification = item.getProductSpecification();
                orderProduct.setBarcode(productSpecification.getBarcode());
                orderProduct.setTotalWeight(productSpecification.getWeight());
                orderProduct.setSpecificationId(productSpecification.getId());
                //设置上架信息
                //如果存在特价就用特价
                Integer price = Objects.isNull(item.getPrice()) ? item.getOriginalPrice() : item.getPrice();
                orderProduct.setDiscountPrice((int) Calculator.mul(price, orderProduct.getProductQty()));//去除优惠有单品总价
                orderProduct.setTotalPrice((int) Calculator.mul(item.getOriginalPrice(), orderProduct.getProductQty()));//单品总价
                orderProduct.setId(null);
                orderProduct.setProductName(item.getName());
            })
        );
        ResponseEntity<OrderDetailResult> orderDetailResultResponse = orderServiceFeign.createOrder(orderParam);
        Tips<OrderDetailResult> orderDetailResultTips = FeginResponseTools.convertResponse(orderDetailResultResponse);
        if (orderDetailResultTips.err()) {
            return ResponseEntity.badRequest().body(orderDetailResultTips);
        }
        //TODO mq设置三十分钟失效
        return ResponseEntity.ok(orderDetailResultTips);
    }

    @DeleteMapping("/orders/{orderCode}")
    @ApiOperation("和色果膳--取消订单")
    @ApiImplicitParam(paramType = "path", name = "orderCode", dataType = "String", required = true, value = "订单id")
    public ResponseEntity<Tips> createOrder(@Valid @NotBlank @PathVariable String orderCode) {
        Tips tips = FeginResponseTools.convertResponse(orderServiceFeign.updateOrderStatus(orderCode, OrderStatus.FAILURE));
        if (tips.err()){
            return ResponseEntity.badRequest().body(tips);
        }
        return ResponseEntity.ok(tips);
    }
}
