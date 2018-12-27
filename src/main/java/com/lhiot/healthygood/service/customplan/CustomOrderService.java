package com.lhiot.healthygood.service.customplan;

import com.leon.microx.id.Generator;
import com.leon.microx.util.Calculator;
import com.leon.microx.util.Jackson;
import com.leon.microx.util.StringUtils;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.lhiot.dc.dictionary.DictionaryClient;
import com.lhiot.dc.dictionary.module.Dictionary;
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
import com.lhiot.healthygood.mq.HealthyGoodQueue;
import com.lhiot.healthygood.service.doctor.DoctorAchievementLogService;
import com.lhiot.healthygood.service.order.OrderService;
import com.lhiot.healthygood.service.user.FruitDoctorService;
import com.lhiot.healthygood.type.CustomOrderDeliveryStatus;
import com.lhiot.healthygood.type.CustomOrderStatus;
import com.lhiot.healthygood.type.OperStatus;
import com.lhiot.healthygood.type.ReceivingWay;
import com.lhiot.healthygood.util.FeginResponseTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    private final OrderService orderService;
    private final BaseDataServiceFeign baseDataServiceFeign;
    private final OrderServiceFeign orderServiceFeign;
    private final ThirdpartyServerFeign thirdpartyServerFeign;
    private final CustomPlanSpecificationMapper customPlanSpecificationMapper;
    private final CustomOrderMapper customOrderMapper;
    private final CustomOrderPauseMapper customOrderPauseMapper;
    private final CustomOrderDeliveryMapper customOrderDeliveryMapper;
    private final Generator<Long> generator;
    private final DictionaryClient dictionaryClient;
    private final FruitDoctorService fruitDoctorService;
    private final RabbitTemplate rabbitTemplate;
    //暂停开始结束时间
    private static final LocalTime PAUSE_TIME = LocalTime.parse("00:00:00");
    //private static final LocalTime END_PAUSE_OF_DAY = LocalTime.parse("23:59:59");
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    public CustomOrderService(CustomPlanService customPlanService,
                              OrderService orderService,
                              BaseDataServiceFeign baseDataServiceFeign,
                              OrderServiceFeign orderServiceFeign,
                              ThirdpartyServerFeign thirdpartyServerFeign,
                              CustomOrderMapper customOrderMapper,
                              CustomPlanSpecificationMapper customPlanSpecificationMapper,
                              CustomOrderPauseMapper customOrderPauseMapper,
                              CustomOrderDeliveryMapper customOrderDeliveryMapper,
                              Generator<Long> generator,
                              DictionaryClient dictionaryClient,
                              DoctorAchievementLogService doctorAchievementLogService, FruitDoctorService fruitDoctorService, RabbitTemplate rabbitTemplate) {
        this.customPlanService = customPlanService;
        this.orderService = orderService;
        this.baseDataServiceFeign = baseDataServiceFeign;
        this.orderServiceFeign = orderServiceFeign;
        this.thirdpartyServerFeign = thirdpartyServerFeign;
        this.customOrderMapper = customOrderMapper;
        this.customPlanSpecificationMapper = customPlanSpecificationMapper;
        this.customOrderPauseMapper = customOrderPauseMapper;
        this.customOrderDeliveryMapper = customOrderDeliveryMapper;
        this.generator = generator;
        this.dictionaryClient = dictionaryClient;
        this.fruitDoctorService = fruitDoctorService;
        this.rabbitTemplate = rabbitTemplate;
        //初始化调用修改暂停恢复定制计划每五分钟调用一次
        HealthyGoodQueue.DelayQueue.UPDATE_CUSTOM_ORDER_STATUS.send(rabbitTemplate, "nothing", 1 * 60 * 1000);
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
        Date current = Date.from(Instant.now());
        String orderCode = generator.get(0, "HGCP");
        customOrder.setCustomOrderCode(orderCode);
        customOrder.setStatus(CustomOrderStatus.WAIT_PAYMENT);
        customOrder.setCreateAt(current);
        customOrder.setPrice(customPlanSpecification.getPrice());
        customOrder.setRemainingQty(customPlanSpecification.getPlanPeriod());//剩余配送次数就是周期数
        customOrder.setQuantity(customPlanSpecification.getQuantity());
        customOrder.setTotalQty(customPlanSpecification.getPlanPeriod());//总配送次数
        customOrder.setDescription(customPlanSpecification.getDescription());//定制计划规格描述
        Optional<Dictionary> optional = dictionaryClient.dictionary("customPlanMaxExtractionDay");
        //7 周套餐设置 30 月套餐设定
        int maxExtractionDay = 0;
        if (optional.get().hasEntry(customPlanSpecification.getPlanPeriod().toString())) {
            maxExtractionDay = Integer.valueOf(optional.get().entry(customPlanSpecification.getPlanPeriod().toString()).get().getName());
        }
        LocalDateTime endExtractionAt = LocalDateTime.now().plusDays(maxExtractionDay);
        customOrder.setEndExtractionAt(Date.from(endExtractionAt.atZone(ZoneId.systemDefault()).toInstant()));//定制提取截止时间
        int result = customOrderMapper.create(customOrder);
        if (result > 0) {
            //mq设置三十分钟未支付失效
            HealthyGoodQueue.DelayQueue.CANCEL_CUSTOM_ORDER.send(rabbitTemplate, orderCode, 30 * 60 * 1000);
            //mq超过最后提取期限，给用户原路按照定制均价退款
            long delay = customOrder.getEndExtractionAt().getTime() - current.getTime();//最后提取时间毫秒数-当前时间毫秒数
            HealthyGoodQueue.DelayQueue.LAST_EXTRACTION.send(rabbitTemplate, orderCode, delay);
            return customOrder;
        }
        return null;
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


    /**
     * 修改定制订单记录
     *
     * @param customOrder
     * @return
     */
    public int updateByCode(CustomOrder customOrder) {
        return customOrderMapper.updateByCode(customOrder);
    }

    /**
     * 暂停配送
     *
     * @param customOrderPause
     * @return
     */
    public Tips pauseCustomOrder(CustomOrderPause customOrderPause) {

        //暂停定制订单
        String customOrderCode = customOrderPause.getCustomOrderCode();
        CustomOrder customOrder = customOrderMapper.selectByCode(customOrderCode);
        if (Objects.isNull(customOrder)) {
            return Tips.warn("未找到定制订单");
        }

        //如果当前时间段内还有暂停配送记录，就不允许操作暂停
        customOrderPause.setCreateAt(Date.from(Instant.now()));//创建时间
        //暂停开始日期
        LocalDate pauseBegin = LocalDate.parse(customOrderPause.getPauseBegin(), dateTimeFormatter);
        //暂停开始时间
        LocalDateTime begin = pauseBegin.atTime(PAUSE_TIME);
        //计划暂停结束时间
        LocalDateTime last = pauseBegin.plusDays(customOrderPause.getPlanPauseDay()).atTime(PAUSE_TIME);
        Date lastDate = Date.from(last.atZone(ZoneId.systemDefault()).toInstant());

        //查询当前设置暂停时间是否已经存在了 如果存在不允许设置
        CustomOrderPause customOrderPauseParam = new CustomOrderPause();
        customOrderPauseParam.setPauseBeginAt(Date.from(begin.atZone(ZoneId.systemDefault()).toInstant()));//暂停开始时间
        customOrderPauseParam.setPlanPauseEndAt(lastDate);//计划暂停结束时间
        customOrderPauseParam.setOperStatus(OperStatus.PAUSE);//暂停状态
        customOrderPauseParam.setCustomOrderCode(customOrderCode);
        if (Objects.nonNull(customOrderPauseMapper.selectCustomOrderPause(customOrderPauseParam))) {
            return Tips.warn("当前设置与已暂停时间段冲突");
        }
        //查询已经使用暂停天数
        Integer hadPauseDays = customOrderPauseMapper.selectHadPauseDays(customOrderCode);
        //未找到记录说明未有暂停设置，已使用暂停天数为0
        if (Objects.isNull(hadPauseDays)) {
            hadPauseDays = 0;
        }
        //数据字典配置的最大暂停天数
        Dictionary dictionary = customPlanService.customPlanMaxPauseDay();
        List<Dictionary.Entry> items = dictionary.getEntries();
        Dictionary.Entry needPlanMaxPauseDay = null;
        for (Dictionary.Entry item : items) {
            //周期=定制总天数
            if (Objects.equals(Integer.valueOf(item.getCode()), customOrder.getTotalQty())) {
                needPlanMaxPauseDay = item;
                break;
            }
        }
        if (Objects.isNull(needPlanMaxPauseDay)) {
            return Tips.warn("未配置最大暂停天数");
        }
        //字典设置的最大允许暂停天数
        Integer allowMaxPause = Integer.valueOf(needPlanMaxPauseDay.getName());
        //最大暂停天数<已经暂停天数+计划设置暂停天数
        if (allowMaxPause < (hadPauseDays + customOrderPause.getPlanPauseDay())) {
            return Tips.warn("已经超过最大暂停天数");
        }
        //默认所有暂停结束设置都为计划的设置，如果恢复，那么再修改实际结束时间
        customOrderPause.setPauseBeginAt(Date.from(begin.atZone(ZoneId.systemDefault()).toInstant()));//暂停开始时间
        customOrderPause.setPauseEndAt(lastDate);//实际暂停结束时间
        customOrderPause.setPlanPauseEndAt(lastDate);//计划暂停结束时间
        customOrderPause.setPauseDay(customOrderPause.getPlanPauseDay());
        customOrderPause.setOperStatus(OperStatus.PAUSE);
        int saveResult = customOrderPauseMapper.create(customOrderPause);
        //保持暂停记录
        return saveResult > 0 ? Tips.info("设置成功") : Tips.warn("设置失败");
    }

    /**
     * 恢复配送
     *
     * @param customOrderCode
     * @return
     */
    public int resumeCustomOrder(String customOrderCode) {
        CustomOrder searchCustomOrder = this.selectByCode(customOrderCode);
        if (Objects.isNull(searchCustomOrder))
            return 0;
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDate currentDate = LocalDate.now();
        Date current = Date.from(currentDateTime.atZone(ZoneId.systemDefault()).toInstant());
        CustomOrderPause customOrderPauseParam = new CustomOrderPause();
        customOrderPauseParam.setPauseBeginAt(current);//暂停开始时间条件
        customOrderPauseParam.setPlanPauseEndAt(current);//计划暂停结束时间条件
        customOrderPauseParam.setOperStatus(OperStatus.PAUSE);//暂停状态
        customOrderPauseParam.setCustomOrderCode(customOrderCode);
        //查找到当前是否设置了暂停 如果有就恢复并修改实际结束时间
        CustomOrderPause customOrderPause = customOrderPauseMapper.selectCustomOrderPause(customOrderPauseParam);
        if (Objects.nonNull(customOrderPause)) {
            //恢复
            customOrderPause.setOperStatus(OperStatus.RECOVERY);
            //实际暂停天数
            LocalDate pauseBeginAt = LocalDateTime.ofInstant(customOrderPause.getPauseBeginAt().toInstant(), ZoneId.systemDefault()).toLocalDate();
            customOrderPause.setPauseEndAt(current);
            //计算实际暂停天数(当前日期-开始暂停日期)
            long pauseDay = currentDate.toEpochDay() - pauseBeginAt.toEpochDay();
            //存在恢复时间还未到达计划开始暂停时间，那么暂停天数为0
            if (pauseDay < 0) {
                pauseDay = 0;
            }
            customOrderPause.setPauseDay(pauseDay);
            //恢复
            return customOrderPauseMapper.update(customOrderPause);
        }
        return 0;
    }

    /**
     * 提取定制计划中的套餐
     *
     * @param customOrder
     * @param deliveryTime eg:{"display":"08:30-09:30","startTime":"2018-12-12 08:30:00","endTime":"2018-12-12 09:30:00"}
     * @param remark       订单备注
     * @return
     */
    public Tips extraction(CustomOrder customOrder, String deliveryTime, String remark) {
        CreateOrderParam orderParam = new CreateOrderParam();
        orderParam.setUserId(customOrder.getUserId());//设置业务用户id
        orderParam.setApplicationType(ApplicationType.HEALTH_GOOD);
        orderParam.setAddress(customOrder.getDeliveryAddress());//定制收货地址
        orderParam.setContactPhone(customOrder.getContactPhone());
        orderParam.setNickname(customOrder.getReceiveUser());
        orderParam.setReceiveUser(customOrder.getReceiveUser());
        orderParam.setReceivingWay(ReceivingWay.TO_THE_HOME);//所有的都是送货上门
        orderParam.setAllowRefund(AllowRefund.NO);//允许退货
        orderParam.setOrderType(OrderType.CUSTOM);//定制订单
        orderParam.setRemark(remark);

        orderParam.setDeliverAt(deliveryTime);

        String storeCode = customOrder.getStoreCode();//门店编码
        //判断门店是否存在
        ResponseEntity<Store> storeResponseEntity = baseDataServiceFeign.findStoreByCode(storeCode, ApplicationType.HEALTH_GOOD);
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
        Tips<ProductShelf> productShelfTips = FeginResponseTools.convertResponse(baseDataServiceFeign.singleShelf(customPlanProductResult.getProductShelfId(), true));
        if (productShelfTips.err()) {
            return productShelfTips;
        }
        ProductShelf productShelf = productShelfTips.getData();
        //查询门店h4库存信息
        Tips<Map<String, Object>> quserSkuTips = FeginResponseTools.convertResponse(thirdpartyServerFeign.querySku(storeCode,
                new String[]{productShelf.getProductSpecification().getBarcode()}));
        //查询门店库存错误
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
        int price = (int) Calculator.div(customOrder.getPrice(), customOrder.getTotalQty());//总定制计划价格/定制周期 计算结果为金额平均值
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

        //本地修改提取次数
        fruitDoctorService.calculationCommission(orderDetailResultTips.getData());//鲜果师业绩
        String orderCode = orderDetailResultTips.getData().getCode();
        CustomOrderDelivery customOrderDelivery = new CustomOrderDelivery();
        customOrderDelivery.setProductShelfId(customPlanProductResult.getProductShelfId());
        customOrderDelivery.setCreateAt(Date.from(Instant.now()));
        customOrderDelivery.setDeliveryTime(deliveryTime);
        customOrderDelivery.setDeliveryAddress(customOrder.getDeliveryAddress());
        customOrderDelivery.setDeliveryStatus(CustomOrderDeliveryStatus.DISPATCHING);//配送中
        customOrderDelivery.setOrderCode(orderCode);
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
            //剩余次数-1 实际为只要这个值不为空，那么就会更新为剩余次数-1
            updateCustomOrder.setRemainingQty(customOrder.getRemainingQty() - 1);
            int updateCustomOrderResult = this.updateByCode(updateCustomOrder);
            log.info("创建定制订单提取记录修改定制订单次数返回结果{}", updateCustomOrderResult);

            //延迟发送海鼎
            orderService.delaySendToHd(orderCode, Jackson.object(deliveryTime, DeliverTime.class));
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
     * @param customOrder 定制订单
     * @param needChild   是否查询子集
     * @return
     */
    public Pages<CustomOrder> customOrderListByStatus(CustomOrder customOrder, boolean needChild) {
        //查询购买的定制计划列表
        List<CustomOrder> customOrderList = customOrderMapper.pageCustomOrder(customOrder);
        if (needChild) {
            customOrderList.forEach(item -> {
                item.setCustomOrderDeliveryList(customOrderDeliveryMapper.selectByCustomOrderId(item.getPlanId(), item.getId()));//设置定制配送记录
                item.setCustomPlan(customPlanService.findById(item.getPlanId()));//设置定制计划
            });
        }
        boolean pageFlag = Objects.nonNull(customOrder.getPage()) && Objects.nonNull(customOrder.getRows()) && customOrder.getPage() > 0 && customOrder.getRows() > 0;
        int total = pageFlag ? customOrderMapper.pageCustomOrderCounts(customOrder) : customOrderList.size();
        return Pages.of(total, customOrderList);
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
        if (Objects.isNull(customOrder)) {
            return Tips.warn("未找到定制订单");
        }
        if (needChlid && Objects.nonNull(customOrder)) {
            List<CustomOrderDelivery> customOrderDeliveryList = customOrderDeliveryMapper.selectByCustomOrderId(customOrder.getPlanId(), customOrder.getId());
            // 设置定制配送记录
            customOrder.setCustomOrderDeliveryList(customOrderDeliveryList);
            // 设置定制计划
            customOrder.setCustomPlan(customPlanService.findById(customOrder.getPlanId()));
            // 查找定制计划套餐详情
            List<Long> shelfIdList = customOrderDeliveryList.stream().map(CustomOrderDelivery::getProductShelfId).collect(Collectors.toList());
            String shelfIds = StringUtils.collectionToDelimitedString(shelfIdList, ",");
            ProductShelfParam productShelfParam = new ProductShelfParam();
            productShelfParam.setIds(shelfIds);
            ResponseEntity<Pages<ProductShelf>> productEntity = baseDataServiceFeign.searchProductShelves(productShelfParam);
            if (productEntity.getStatusCode().isError()) {
                return Tips.warn(productEntity.getBody().toString());
            }
            List<ProductShelf> productShelfList = productEntity.getBody().getArray();
            productShelfList.forEach(productShelf ->
                    customOrderDeliveryList.forEach(customOrderDelivery -> {
                        if (Objects.equals(productShelf.getId(), customOrderDelivery.getProductShelfId())) {
                            customOrderDelivery.setProductName(productShelf.getName());
                            customOrderDelivery.setImage(productShelf.getImage());
                        }
                    })
            );
        }
        Tips<CustomOrder> tips = new Tips<>();
        tips.setData(customOrder);
        return tips;
    }

    /**
     * 查询定制订单总记录数
     *
     * @param customOrder
     * @return
     */
    public int count(CustomOrder customOrder) {
        return this.customOrderMapper.pageCustomOrderCounts(customOrder);
    }

    /**
     * 查询定制订单总记录
     *
     * @param customOrder
     * @return
     */
    public Pages<CustomOrder> pageList(CustomOrder customOrder) {
        int total = 0;
        if (customOrder.getRows() != null && customOrder.getRows() > 0) {
            total = this.count(customOrder);
        }
        return Pages.of(total, this.customOrderMapper.pageCustomOrder(customOrder));
    }

    /**
     * 每天自动提取定制订单
     * 被调用者 (自动提取类型的已支付(余额，微信回调)操作，修改配送时间操作)
     * 注：当天下单当天不会配送，第二天才能配送
     * 注：自动配送会允许修改配送时间
     *
     * @param deliveryDateTime 下一次配送具体时间
     * @param customOrderCode  定制订单编码
     */
    public void scheduleDeliveryCustomOrder(LocalDateTime deliveryDateTime, String customOrderCode) {
        CustomOrder customOrder = customOrderMapper.selectByCode(customOrderCode);
        if (Objects.isNull(customOrder) || customOrder.getRemainingQty() <= 0 || !Objects.equals(customOrder.getStatus(), CustomOrderStatus.CUSTOMING)) {
            log.info("每天发订单配送,定制订单不存在或者定制订单剩余次数为0或者定制订单暂停中{}", customOrderCode);
            return;
        }
        LocalDateTime current = LocalDateTime.now();
        //计算下一次配送的时间与当前时间的毫秒数，做为消费端处理配送时候的时间
        long interval = deliveryDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - current.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        //等待到下一次配送的时间处理提取
        //发送自动提取定制订单延迟队列
        HealthyGoodQueue.DelayQueue.AUTO_EXTRACTION.send(rabbitTemplate, customOrderCode, interval);
    }

    public CustomOrderDelivery selectOrderCode(String orderCode) {
        return customOrderDeliveryMapper.selectOrderCode(orderCode);
    }

    public void updateById(CustomOrder customOrder) {
        customOrderMapper.updateById(customOrder);
    }

    /**
     * 售后 退还定制订单剩余次数+1
     * 不退款
     *
     * @return
     */
    public Tips refundCustomOrderDelivery(String orderCode) {
        //如果是定制订单，需要退剩余次数+1
        //定制订单 只退用户剩余次数，不退款
        CustomOrderDelivery customOrderDelivery = this.selectOrderCode(orderCode);
        if (Objects.isNull(customOrderDelivery)) {
            log.error("订单退货,找不到提取的定制配送记录,{}", orderCode);
            return Tips.warn("找不到提取的定制配送记录");
        }
        CustomOrder customOrder = new CustomOrder();
        customOrder.setId(customOrderDelivery.getCustomOrderId());
        customOrder.setRemainingQtyAdd(1);//退还一次剩余
        this.updateById(customOrder);
        return Tips.info("退还剩余次数成功");
    }

}

