package com.lhiot.healthygood.api.commons;

import com.leon.microx.predefine.OnOff;
import com.leon.microx.util.Position;
import com.leon.microx.util.StringUtils;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.leon.microx.web.session.Sessions;
import com.leon.microx.web.swagger.ApiParamType;
import com.lhiot.healthygood.domain.customplan.CustomOrderTime;
import com.lhiot.healthygood.feign.BaseDataServiceFeign;
import com.lhiot.healthygood.feign.DeliverServiceFeign;
import com.lhiot.healthygood.feign.model.*;
import com.lhiot.healthygood.feign.type.ApplicationType;
import com.lhiot.healthygood.feign.type.CoordinateSystem;
import com.lhiot.healthygood.feign.type.DeliverAtType;
import com.lhiot.healthygood.service.common.CommonService;
import com.lhiot.healthygood.type.ShelfType;
import com.lhiot.healthygood.util.FeginResponseTools;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 公共接口api /custom-plan-sections
 */
@Api(description = "公共接口")
@Slf4j
@RestController
public class CommonsApi {
    private final CommonService commonService;
    private final DeliverServiceFeign deliverServiceFeign;
    private final BaseDataServiceFeign baseDataServiceFeign;

    private static final LocalTime BEGIN_DELIVER_OF_DAY = LocalTime.parse("08:30:00");
    private static final LocalTime END_DELIVER_OF_DAY = LocalTime.parse("21:30:01");

    private static final DateTimeFormatter HOUR_AND_MIN = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    public CommonsApi(CommonService commonService, DeliverServiceFeign deliverServiceFeign, BaseDataServiceFeign baseDataServiceFeign) {
        this.commonService = commonService;
        this.deliverServiceFeign = deliverServiceFeign;
        this.baseDataServiceFeign = baseDataServiceFeign;
    }

    //获取配送时间列表 定制订单使用
    @Sessions.Uncheck
    @GetMapping("/custom-plan-delivery/times")
    @ApiOperation(value = "获取订单配送时间 定制订单使用")
    public ResponseEntity<List<CustomOrderTime>> times() {
        List<CustomOrderTime> times = new ArrayList<>();

        LocalDateTime begin = LocalDate.now().atTime(BEGIN_DELIVER_OF_DAY);
        LocalDateTime latest = LocalDate.now().atTime(END_DELIVER_OF_DAY);
        LocalDateTime current = begin.withMinute(30);

        while (latest.compareTo(current) >= 0) {
            LocalDateTime next = current.plusHours(1);
            String display = StringUtils.format("{}-{}", current.format(HOUR_AND_MIN), next.format(HOUR_AND_MIN));
            times.add(CustomOrderTime.of(display, current.toLocalTime(), next.toLocalTime()));
            current = next;
        }
        return ResponseEntity.ok(times);
    }

    @Sessions.Uncheck
    @GetMapping("/delivery/times")
    @ApiOperation(value = "获取配送时间列表")
    public ResponseEntity<Map<String, Object>> getDeliverTime() {
        //查询今天和明天的配送时间列表
        return deliverServiceFeign.deliverTimes(null);
    }

    @Sessions.Uncheck
    @ApiOperation(value = "计算配送距离（收获地址与智能选择最近的门店）", response = Store.class, responseContainer = "List")
    @GetMapping("/delivery/distance")
    public ResponseEntity nearStore(@RequestParam(value = "address", required = false) String address, @RequestParam(value = "lng" , required = false) Double lng, @RequestParam(value = "lat", required = false) Double lat) {
        Pages<Store> storeResult = commonService.nearStore(address, lng, lat);
        if (Objects.isNull(storeResult)) {
            return ResponseEntity.badRequest().body("未找到门店");
        }
        return ResponseEntity.ok(storeResult);
    }

    @Sessions.Uncheck
    @ApiOperation("计算配送费")
    @ApiImplicitParam(paramType = ApiParamType.BODY, name = "feeQuery", value = "配送费计算传入参数", dataType = "DeliverFeeQuery", required = true)
    @PostMapping("/delivery/fee/search")
    public ResponseEntity search(@RequestBody DeliverFeeQuery feeQuery){
        String storeCode = feeQuery.getStoreCode();
        //判断门店是否存在
        ResponseEntity storeResponseEntity = baseDataServiceFeign.findStoreByCode(storeCode, ApplicationType.HEALTH_GOOD);
        if (Objects.isNull(storeResponseEntity) || storeResponseEntity.getStatusCode().isError()) {
            return storeResponseEntity;
        }
        Store store = (Store)storeResponseEntity.getBody();
        feeQuery.setStoreId(store.getId());//设置门店id
        feeQuery.setDeliverAtType(DeliverAtType.ALL_DAY);
        //如果有地址依据地址设置经纬度
        if(StringUtils.isNotBlank(feeQuery.getAddress())){
            Position.GCJ02 position = commonService.getPositionFromAddres(feeQuery.getAddress());//地址转经纬度
            if (Objects.isNull(position)) {
                log.error("查询收货地址经纬度信息失败{}", feeQuery.getAddress());
                //不做处理
            } else {
                feeQuery.setTargetLat(position.getLatitude());
                feeQuery.setTargetLng(position.getLongitude());
            }
        }
        feeQuery.setApplicationType(ApplicationType.HEALTH_GOOD);
        feeQuery.setCoordinateSystem(CoordinateSystem.AMAP);//腾讯高德系
        //依据上架ids查询上架商品信息
        Object[] shelfIds = feeQuery.getOrderProducts().parallelStream().map(OrderProduct::getShelfId).toArray(Object[]::new);

        ProductShelfParam productShelfParam = new ProductShelfParam();
        productShelfParam.setIds(StringUtils.arrayToCommaDelimitedString(shelfIds));
        productShelfParam.setShelfStatus(OnOff.ON);
        productShelfParam.setIncludeProduct(true);
        productShelfParam.setShelfType(ShelfType.NORMAL);
        //查找基础服务上架商品信息
        Tips<Pages<ProductShelf>> productShelfTips = FeginResponseTools.convertResponse(baseDataServiceFeign.searchProductShelves(productShelfParam));
        if (productShelfTips.err()) {
            return ResponseEntity.badRequest().body(productShelfTips.getMessage());
        }
        List<OrderProduct> orderProductList = feeQuery.getOrderProducts();

        AtomicReference<Integer> productAmount = new AtomicReference<>(0);
        AtomicReference<BigDecimal> weight = new AtomicReference<>(BigDecimal.ZERO);
        //给商品赋值规格数量
        orderProductList.forEach(orderProduct -> productShelfTips.getData().getArray().stream()
                //上架id相同的订单商品信息，通过基础服务获取的赋值给订单商品信息
                .filter(productShelf -> Objects.equals(orderProduct.getShelfId(), productShelf.getId()))
                .forEach(item -> {
                    BigDecimal quty = new BigDecimal(orderProduct.getProductQty());
                    weight.updateAndGet(v -> v.add(item.getProductSpecification().getWeight().multiply(quty)));
                    productAmount.updateAndGet(v -> v + quty.multiply(new BigDecimal(Objects.isNull(item.getPrice())?item.getOriginalPrice():item.getPrice())).intValue());
                })
        );
        feeQuery.setWeight(weight.get().doubleValue());
        feeQuery.setOrderFee(productAmount.get());
        //查询配送中心配送费
        ResponseEntity responseEntity = deliverServiceFeign.search(feeQuery);
        if(Objects.nonNull(responseEntity) && responseEntity.getStatusCode().is2xxSuccessful()){
            return ResponseEntity.ok(responseEntity.getBody());
        }
        return responseEntity;
    }
}
