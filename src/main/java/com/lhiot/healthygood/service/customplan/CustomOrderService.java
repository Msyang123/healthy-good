package com.lhiot.healthygood.service.customplan;

import com.leon.microx.id.Generator;
import com.leon.microx.util.Calculator;
import com.leon.microx.util.Jackson;
import com.leon.microx.util.StringUtils;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.lhiot.healthygood.domain.customplan.*;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanDetailResult;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanPeriodResult;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanProductResult;
import com.lhiot.healthygood.feign.BaseDataServiceFeign;
import com.lhiot.healthygood.feign.OrderServiceFeign;
import com.lhiot.healthygood.feign.ThirdpartyServerFeign;
import com.lhiot.healthygood.feign.model.*;
import com.lhiot.healthygood.feign.type.AllowRefund;
import com.lhiot.healthygood.feign.type.ApplicationType;
import com.lhiot.healthygood.feign.type.OrderType;
import com.lhiot.healthygood.mapper.customplan.CustomOrderDeliveryMapper;
import com.lhiot.healthygood.mapper.customplan.CustomOrderMapper;
import com.lhiot.healthygood.mapper.customplan.CustomOrderPauseMapper;
import com.lhiot.healthygood.mapper.customplan.CustomPlanSpecificationMapper;
import com.lhiot.healthygood.type.CustomOrderDeliveryStatus;
import com.lhiot.healthygood.type.CustomOrderStatus;
import com.lhiot.healthygood.type.ReceivingWay;
import com.lhiot.healthygood.util.FeginResponseTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Description:定制订单服务类
 *
 * @author yj
 * @date 2018/11/26
 */
@Service
@Transactional
@Slf4j
public class CustomOrderService {

    private final CustomPlanService customPlanService;
    private final BaseDataServiceFeign baseDataServiceFeign;
    private final OrderServiceFeign orderServiceFeign;
    private final ThirdpartyServerFeign thirdpartyServerFeign;
    private final CustomPlanSpecificationMapper customPlanSpecificationMapper;
    private final CustomOrderMapper customOrderMapper;
    private final CustomOrderPauseMapper customOrderPauseMapper;
    private final CustomOrderDeliveryMapper customOrderDeliveryMapper;
    private final Generator<Long> generator;
    //暂停开始结束时间
    private static final LocalTime BEGIN_PAUSE_OF_DAY = LocalTime.parse("00:00:00");
    private static final LocalTime END_PAUSE_OF_DAY = LocalTime.parse("23:59:59");
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    public CustomOrderService(CustomPlanService customPlanService,
                              BaseDataServiceFeign baseDataServiceFeign,
                              OrderServiceFeign orderServiceFeign,
                              ThirdpartyServerFeign thirdpartyServerFeign,
                              CustomOrderMapper customOrderMapper,
                              CustomPlanSpecificationMapper customPlanSpecificationMapper,
                              CustomOrderPauseMapper customOrderPauseMapper,
                              CustomOrderDeliveryMapper customOrderDeliveryMapper,
                              Generator<Long> generator
    ) {
        this.customPlanService = customPlanService;
        this.baseDataServiceFeign = baseDataServiceFeign;
        this.orderServiceFeign = orderServiceFeign;
        this.thirdpartyServerFeign = thirdpartyServerFeign;
        this.customOrderMapper = customOrderMapper;
        this.customPlanSpecificationMapper = customPlanSpecificationMapper;
        this.customOrderPauseMapper = customOrderPauseMapper;
        this.customOrderDeliveryMapper = customOrderDeliveryMapper;
        this.generator = generator;
    }

    /**
     * 新增定制计划订单
     *
     * @param customOrder 定制计划与版块关系对象
     * @return 定制计划与版块关系Id
     */
    public CustomOrder createCustomOrder(CustomOrder customOrder) {
        CustomPlanSpecification customPlanSpecification = customPlanSpecificationMapper.selectById(customOrder.getSpecificationId());//查找指定定制规格
        if (Objects.isNull(customPlanSpecification))
            return null;
        //和色果膳定制计划订单号
        String orderCode = generator.get(0, "HGCP");
        customOrder.setCustomOrderCode(orderCode);
        customOrder.setStatus(CustomOrderStatus.WAIT_PAYMENT);
        customOrder.setCreateAt(Date.from(Instant.now()));
        customOrder.setPrice(customPlanSpecification.getPrice());
        customOrder.setRemainingQty(customPlanSpecification.getPlanPeriod());//剩余配送次数就是周期数
        customOrder.setQuantity(customPlanSpecification.getQuantity());
        customOrder.setTotalQty(customPlanSpecification.getPlanPeriod());//总配送次数
        customOrder.setDescription(customPlanSpecification.getDescription());//定制计划规格描述
        int result = customOrderMapper.create(customOrder);

        return result > 0 ? customOrder : null;
    }

    /**
     * 查询指定定制订单
     *
     * @param code
     * @return
     */
    public CustomOrder selectByCode(String code) {
        return customOrderMapper.selectByCode(code);
    }


    public int updateByCode(CustomOrder customOrder, CustomOrderPause customOrderPause) {
        CustomOrder searchCustomOrder = this.selectByCode(customOrder.getCustomOrderCode());
        if (Objects.isNull(searchCustomOrder))
            return 0;
        switch (customOrder.getStatus()) {
            //恢复配送
            case CUSTOMING:
                customOrderPauseMapper.deleteByCustomOrderId(searchCustomOrder.getId());
                break;
            //暂停配送 todo 需要检查暂停时间规则
            case PAUSE_DELIVERY:
                //如果当前还有暂停配送记录，就不允许操作暂停(定时任务自动恢复配送，会删除掉已经过期的暂停记录)
                if (Objects.nonNull(customOrderPauseMapper.selectByCustomOrderId(searchCustomOrder.getId()))) {
                    return 0;
                }
                customOrderPause.setCreateAt(Date.from(Instant.now()));//创建时间
                customOrderPause.setCustomOrderId(searchCustomOrder.getId());//定制计划id
                //暂停开始日期
                LocalDate pauseBegin = LocalDate.parse(customOrderPause.getPauseBegin(), dateTimeFormatter);
                //暂停开始时间
                LocalDateTime begin = pauseBegin.atTime(BEGIN_PAUSE_OF_DAY);
                //暂停结束时间
                LocalDateTime last = pauseBegin.plusDays(customOrderPause.getPauseDay()).atTime(END_PAUSE_OF_DAY);
                customOrderPause.setPauseBeginAt(Date.from(begin.atZone(ZoneId.systemDefault()).toInstant()));//暂停开始时间
                customOrderPause.setPauseEndAt(Date.from(last.atZone(ZoneId.systemDefault()).toInstant()));//暂停结束时间
                break;
            default:
                break;
        }
        return customOrderMapper.updateByCode(customOrder);
    }

    /**
     * 提取定制计划中的套餐
     *
     * @param customOrder
     * @param remark      订单备注
     * @return
     */
    public Tips extraction(CustomOrder customOrder, String remark) {
        CreateOrderParam orderParam = new CreateOrderParam();
        orderParam.setUserId(customOrder.getUserId());//设置业务用户id
        orderParam.setApplicationType(ApplicationType.HEALTH_GOOD);
        orderParam.setAddress(customOrder.getDeliveryAddress());//定制收货地址
        orderParam.setContactPhone(customOrder.getContactPhone());
        orderParam.setNickname(customOrder.getReceiveUser());
        orderParam.setReceiveUser(customOrder.getReceiveUser());
        orderParam.setReceivingWay(ReceivingWay.TO_THE_HOME);//所有的都是送货上门
        orderParam.setAllowRefund(AllowRefund.YES);//允许退货
        orderParam.setOrderType(OrderType.CUSTOM);//定制订单
        orderParam.setRemark(remark);
        String deliveryTime = customOrder.getDeliveryTime();
        //TODO 需要自己调整
        LocalDate pauseBegin = LocalDate.now();//当天
        deliveryTime = deliveryTime.replace("{", "{" + pauseBegin.format(dateTimeFormatter) + " ");
        orderParam.setDeliveryAt(deliveryTime);//购买定制计划的配送时间 eg {12:00:00}-{13:00:00}

        String storeCode = customOrder.getStoreCode();//门店编码
        //判断门店是否存在
        ResponseEntity<Store> storeResponseEntity = baseDataServiceFeign.findStoreByCode(storeCode);
        Tips<Store> storeTips = FeginResponseTools.convertResponse(storeResponseEntity);
        if (storeTips.err()) {
            return storeTips;
        }
        Store store = storeTips.getData();

        //给订单门店赋值
        OrderStore orderStore = new OrderStore();
        orderStore.setStoreId(store.getId());
        orderStore.setStoreCode(store.getCode());
        orderStore.setStoreName(store.getName());
        orderParam.setOrderStore(orderStore);

        //查询定制计划中的第x天的定制套餐
        CustomPlanDetailResult customPlanDetailResult = customPlanService.findDetail(customOrder.getPlanId());
        if (Objects.isNull(customPlanDetailResult))
            return Tips.warn("未找到定制计划");
        List<CustomPlanPeriodResult> customPlanPeriodResultList = customPlanDetailResult.getPeriodList();
        CustomPlanPeriodResult currentOrderPlanPeriod = null;
        //查找当前的购买的定制计划的定制计划规则
        for (CustomPlanPeriodResult item : customPlanPeriodResultList) {
            //找到定制周期与购买天数相同的数据
            if (Objects.equals(item.getPlanPeriod(), customOrder.getTotalQty())) {
                currentOrderPlanPeriod = item;
                break;
            }
        }
        if (Objects.isNull(currentOrderPlanPeriod))
            return Tips.warn("当前购买定制未找到定制计划");

        //查找定制计划中提取第x天的套餐
        List<CustomPlanProductResult> customPlanProductResultList = currentOrderPlanPeriod.getProducts();
        CustomPlanProductResult customPlanProductResult = null;
        for (CustomPlanProductResult item : customPlanProductResultList) {
            if (item.getDayOfPeriod() == (customOrder.getTotalQty() - customOrder.getRemainingQty() + 1)) {
                customPlanProductResult = item;
                break;
            }
        }
        if (Objects.isNull(customPlanProductResult))
            return Tips.warn("未找到定制计划中的套餐");

        //查找基础服务上架商品信息
        Tips<ProductShelf> productShelfTips = FeginResponseTools.convertResponse(baseDataServiceFeign.singleShelf(customPlanProductResult.getProductShelfId(),true));
        if (productShelfTips.err()) {
            return productShelfTips;
        }
        ProductShelf productShelf = productShelfTips.getData();
        //查询门店h4库存信息
        Tips<Map<String, Object>> quserSkuTips = FeginResponseTools.convertResponse(thirdpartyServerFeign.querySku(storeCode,
                new String[]{productShelf.getProductSpecification().getBarcode()}));

        if (quserSkuTips.err()) {
            return quserSkuTips;
        }
        List<Map> businvs = (List<Map>) quserSkuTips.getData().get("businvs");
        //校验库存是否足够
        for (Map businv : businvs) {
            if (Objects.equals(productShelf.getProductSpecification().getBarcode(), businv.get("barCode"))
                    && productShelf.getProductSpecification().getLimitInventory() > Double.valueOf(businv.get("qty").toString())) {
                return Tips.warn(String.format("%s实际库存%s低于安全库存%d，不允许销售", productShelf.getProductSpecification().getBarcode(), businv.get("qty").toString(), productShelf.getProductSpecification().getLimitInventory()));
            }
        }
        //设置订单商品
        int price = (int) Calculator.div(customOrder.getPrice(), customOrder.getTotalQty());//总定制计划价格/定制周期
        List<OrderProduct> orderProductList = new ArrayList<>(1);
        OrderProduct orderProduct = new OrderProduct();
        orderProductList.add(orderProduct);
        orderProduct.setSpecificationId(productShelf.getSpecificationId());
        orderProduct.setBarcode(productShelf.getProductSpecification().getBarcode());
        orderProduct.setTotalPrice(price);
        orderProduct.setDiscountPrice(price);
        orderProduct.setProductQty(customOrder.getQuantity());
        orderProduct.setShelfId(productShelf.getId());
        orderProduct.setShelfQty(productShelf.getShelfQty());
        orderProduct.setTotalWeight(productShelf.getProductSpecification().getWeight());
        orderProduct.setImage(productShelf.getImage());
        orderProduct.setProductName(productShelf.getName());
        orderParam.setOrderProducts(orderProductList);

        //设置订单金额
        orderParam.setDeliveryAmount(0);
        orderParam.setCouponAmount(0);
        orderParam.setTotalAmount(price);
        orderParam.setAmountPayable(price);
        orderParam.setPayId(customOrder.getPayId());//第三方支付id
        //创建已支付订单
        ResponseEntity<OrderDetailResult> orderDetailResultResponse = orderServiceFeign.createPaidOrder(orderParam);

        Tips<OrderDetailResult> orderDetailResultTips = FeginResponseTools.convertResponse(orderDetailResultResponse);
        if (orderDetailResultTips.err()) {
            log.error("提取定制订单失败{}", orderDetailResultTips);
            return orderDetailResultTips;
        }
        //TODO 本地mq延迟到配送时间前一小时发送海鼎
        //本地修改提取次数
        CustomOrderDelivery customOrderDelivery = new CustomOrderDelivery();
        customOrderDelivery.setProductShelfId(customPlanProductResult.getProductShelfId());
        customOrderDelivery.setCreateAt(Date.from(Instant.now()));
        customOrderDelivery.setDeliveryTime(Jackson.json(deliveryTime));
        customOrderDelivery.setDeliveryAddress(customOrder.getDeliveryAddress());
        customOrderDelivery.setDeliveryStatus(CustomOrderDeliveryStatus.DISPATCHING);//配送中
        customOrderDelivery.setOrderCode(orderDetailResultTips.getData().getCode());
        customOrderDelivery.setCustomOrderId(customOrder.getId());
        customOrderDelivery.setCustomPlanProductId(customPlanProductResult.getId());
        customOrderDelivery.setDayOfPeriod(customPlanProductResult.getDayOfPeriod());
        customOrderDelivery.setRemark(remark);
        int createResult = customOrderDeliveryMapper.create(customOrderDelivery);
        log.info("创建定制订单提取记录返回结果{}", createResult);
        if (createResult > 0) {
            CustomOrder updateCustomOrder = new CustomOrder();
            updateCustomOrder.setCustomOrderCode(customOrder.getCustomOrderCode());
            //还剩余一份 说明提取完此次就没有了
            if (customOrder.getRemainingQty() <= 1) {
                updateCustomOrder.setStatus(CustomOrderStatus.FINISHED);
            }
            updateCustomOrder.setRemainingQty(customOrder.getRemainingQty() - 1);
            int updateCustomOrderResult = customOrderMapper.updateByCode(updateCustomOrder);
            log.info("创建定制订单提取记录修改定制订单次数返回结果{}", updateCustomOrderResult);
        }
        return orderDetailResultTips;
    }

    /**
     * 购买定制计划状态数目统计信息
     *
     * @param userId 用户id
     * @return
     */
    public Tips<CustomOrderGroupCount> statusCount(Long userId) {
        Tips<CustomOrderGroupCount> tips = new Tips<>();
        tips.setData(customOrderMapper.statusCount(userId));
        return tips;
    }

    /**
     * 查询用户定制订单
     *
     * @param customOrder    定制订单
     * @param needChlid 是否查询子集
     * @return
     */
    public Pages<CustomOrder> customOrderListByStatus(CustomOrder customOrder, boolean needChlid) {
        //查询购买的定制计划列表
        List<CustomOrder> customOrderList = customOrderMapper.pageCustomOrder(customOrder);
        if (needChlid) {
            customOrderList.forEach(item -> {
                item.setCustomOrderDeliveryList(customOrderDeliveryMapper.selectByCustomOrderId(item.getPlanId(),item.getId()));//设置定制配送记录
                item.setCustomPlan(customPlanService.findById(item.getPlanId()));//设置定制计划
            });
        }
        boolean pageFlag = Objects.nonNull(customOrder.getPage()) && Objects.nonNull(customOrder.getRows()) && customOrder.getPage() > 0 && customOrder.getRows() > 0;
        int total = pageFlag ? customOrderMapper.pageCustomOrderCounts(customOrder) : customOrderList.size();
        return Pages.of(total,customOrderList);
    }

    /**
     * 依据定制订单code查询订单详情
     *
     * @param orderCode
     * @param needChlid
     * @return
     */
    @Nullable
    public Tips<CustomOrder> selectByCode(String orderCode, boolean needChlid) {
        CustomOrder customOrder = customOrderMapper.selectByCode(orderCode);
        if (Objects.isNull(customOrder)){
            return Tips.warn("未找到定制订单");
        }
        if (needChlid && Objects.nonNull(customOrder)) {
            List<CustomOrderDelivery> customOrderDeliveryList = customOrderDeliveryMapper.selectByCustomOrderId(customOrder.getPlanId(),customOrder.getId());
            // 设置定制配送记录
            customOrder.setCustomOrderDeliveryList(customOrderDeliveryList);
            // 设置定制计划
            customOrder.setCustomPlan(customPlanService.findById(customOrder.getPlanId()));
            // 查找定制计划套餐详情
            List<Long> shelfIdList = customOrderDeliveryList.stream().map(CustomOrderDelivery::getProductShelfId).collect(Collectors.toList());
            String shelfIds = StringUtils.collectionToDelimitedString(shelfIdList,",");
            ProductShelfParam productShelfParam = new ProductShelfParam();
            productShelfParam.setIds(shelfIds);
            ResponseEntity<Pages<ProductShelf>> productEntity = baseDataServiceFeign.searchProductShelves(productShelfParam);
            if (productEntity.getStatusCode().isError()) {
                return Tips.warn(productEntity.getBody().toString());
            }
            List<ProductShelf> productShelfList = productEntity.getBody().getArray();
            productShelfList.forEach(productShelf -> {
                customOrderDeliveryList.forEach(customOrderDelivery -> {
                    if (Objects.equals(productShelf.getId(),customOrderDelivery.getProductShelfId())) {
                        customOrderDelivery.setProductName(productShelf.getName());
                        customOrderDelivery.setImage(productShelf.getImage());
                    }
                });
            });
        }
        Tips<CustomOrder> tips = new Tips<>();
        tips.setData(customOrder);
        return tips;
    }

    /**
     * 查询定制订单总记录数
     * @param customOrder
     * @return
     */
    public int count(CustomOrder customOrder) {
        return this.customOrderMapper.pageCustomOrderCounts(customOrder);
    }

    /**
     * 查询定制订单总记录
     * @param customOrder
     * @return
     */
    public Pages<CustomOrder> pageList(CustomOrder customOrder) {
        int total = 0;
        if (customOrder.getRows() != null && customOrder.getRows() > 0 ){
            total = this.count(customOrder);
        }
        return Pages.of(total, this.customOrderMapper.pageCustomOrder(customOrder));
    }

}

