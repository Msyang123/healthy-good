package com.lhiot.healthygood.api.customplan;

import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.leon.microx.web.session.Sessions;
import com.leon.microx.web.swagger.ApiHideBodyProperty;
import com.leon.microx.web.swagger.ApiParamType;
import com.lhiot.healthygood.config.HealthyGoodConfig;
import com.lhiot.healthygood.domain.customplan.CustomOrder;
import com.lhiot.healthygood.domain.customplan.CustomOrderGroupCount;
import com.lhiot.healthygood.domain.customplan.CustomOrderPause;
import com.lhiot.healthygood.feign.BaseUserServerFeign;
import com.lhiot.healthygood.feign.PaymentServiceFeign;
import com.lhiot.healthygood.feign.model.OrderDetailResult;
import com.lhiot.healthygood.feign.model.UserDetailResult;
import com.lhiot.healthygood.feign.model.WxPayModel;
import com.lhiot.healthygood.feign.type.ApplicationType;
import com.lhiot.healthygood.feign.type.SourceType;
import com.lhiot.healthygood.service.customplan.CustomOrderService;
import com.lhiot.healthygood.type.CustomOrderBuyType;
import com.lhiot.healthygood.type.CustomOrderStatus;
import com.lhiot.healthygood.util.FeginResponseTools;
import com.lhiot.healthygood.util.RealClientIp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.Objects;

@Api(description = "购买定制计划")
@Slf4j
@RestController
public class CustomOrderApi {

    private final CustomOrderService customOrderService;
    private final PaymentServiceFeign paymentServiceFeign;
    private final HealthyGoodConfig.WechatPayConfig wechatPayConfig;
    private final BaseUserServerFeign baseUserServerFeign;

    @Autowired
    public CustomOrderApi(CustomOrderService customOrderService, PaymentServiceFeign paymentServiceFeign, HealthyGoodConfig healthyGoodConfig, BaseUserServerFeign baseUserServerFeign) {

        this.customOrderService = customOrderService;
        this.paymentServiceFeign = paymentServiceFeign;
        this.wechatPayConfig = healthyGoodConfig.getWechatPay();
        this.baseUserServerFeign = baseUserServerFeign;
    }

    @PostMapping("/custom-orders")
    @ApiOperation(value = "立即定制-创建定制计划", response = CustomOrder.class)
    @ApiHideBodyProperty({"id", "beginCreateAt", "endCreateAt", "user", "rows", "page"})
    public ResponseEntity create(@Valid @RequestBody CustomOrder customOrder, Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        customOrder.setUserId(userId);
        ResponseEntity<UserDetailResult> userDetailResultResponseEntity = baseUserServerFeign.findById(userId);
        Tips<UserDetailResult> userDetailResultTips = FeginResponseTools.convertResponse(userDetailResultResponseEntity);
        if (!userDetailResultTips.err()) {
            customOrder.setNickname(userDetailResultTips.getData().getNickname());
            customOrder.setPhone(userDetailResultTips.getData().getPhone());
        } else {
            log.warn("立即定制-创建定制计划 查询业务用户信息错误{}", userDetailResultResponseEntity);
        }
        CustomOrder result = customOrderService.createCustomOrder(customOrder);
        if (Objects.isNull(result)) {
            return ResponseEntity.badRequest().body("创建失败");
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/custom-orders/{orderCode}")
    @ApiOperation(value = "取消购买定制计划", response = String.class)
    public ResponseEntity cancel(@PathVariable("orderCode") String orderCode, Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        ResponseEntity validateOrderOwner = validateOrderOwner(userId, orderCode);
        if (validateOrderOwner.getStatusCode().isError()) {
            return validateOrderOwner;
        }
        CustomOrder customOrder = (CustomOrder) validateOrderOwner.getBody();
        if (!Objects.equals(customOrder.getStatus(), CustomOrderStatus.WAIT_PAYMENT))
            return ResponseEntity.badRequest().body("当前定制计划非待支付状态，不能取消");

        CustomOrder updateCustomOrder = new CustomOrder();
        updateCustomOrder.setStatus(CustomOrderStatus.INVALID);
        updateCustomOrder.setCustomOrderCode(orderCode);
        int result = customOrderService.updateByCode(updateCustomOrder);
        return result > 0 ? ResponseEntity.ok("取消成功") : ResponseEntity.badRequest().body("取消失败");
    }

    @PostMapping("/custom-orders/{orderCode}/payment-sign")
    @ApiOperation(value = "定制计划支付时微信签名", response = String.class)
    public ResponseEntity paymentSign(@PathVariable("orderCode") String orderCode, HttpServletRequest request, Sessions.User user) {
        String openId = user.getUser().get("openId").toString();
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        ResponseEntity validateOrderOwner = validateOrderOwner(userId, orderCode);
        if (validateOrderOwner.getStatusCode().isError()) {
            return validateOrderOwner;
        }
        CustomOrder customOrder = (CustomOrder) validateOrderOwner.getBody();
        WxPayModel wxPayModel = new WxPayModel();
        wxPayModel.setApplicationType(ApplicationType.HEALTH_GOOD);
        wxPayModel.setBackUrl(wechatPayConfig.getCustomplanCallbackUrl());
        wxPayModel.setClientIp(RealClientIp.getRealIp(request));//获取客户端真实ip
        wxPayModel.setConfigName(wechatPayConfig.getConfigName());//微信支付简称
        wxPayModel.setFee(customOrder.getPrice());
        wxPayModel.setMemo("定制订单支付");
        wxPayModel.setOpenid(openId);
        wxPayModel.setSourceType(SourceType.CUSTOM_PLAN);
        wxPayModel.setUserId(userId);
        wxPayModel.setAttach(orderCode);//定制订单code
        return paymentServiceFeign.wxJsSign(wxPayModel);
    }


    @PutMapping("/custom-orders/{orderCode}/delivery-pause")
    @ApiOperation(value = "修改个人购买计划状态（暂停配送）", response = String.class)
    public ResponseEntity deliveryPause(@PathVariable("orderCode") String orderCode, @Valid @RequestBody CustomOrderPause customOrderPause, Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        ResponseEntity validateOrderOwner = validateOrderOwner(userId, orderCode);
        if (validateOrderOwner.getStatusCode().isError()) {
            return validateOrderOwner;
        }
        //不需要检测是否暂停中 不依赖定制订单状态
        customOrderPause.setCustomOrderCode(orderCode);
        Tips result = customOrderService.pauseCustomOrder(customOrderPause);
        return result.err() ? ResponseEntity.badRequest().body(result.getMessage()) : ResponseEntity.ok(result.getMessage());
    }


    @PutMapping("/custom-orders/{orderCode}/delivery-recovery")
    @ApiOperation(value = "修改个人购买计划状态（恢复配送）", response = String.class)
    public ResponseEntity deliveryRecovery(@PathVariable("orderCode") String orderCode, Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        ResponseEntity validateOrderOwner = validateOrderOwner(userId, orderCode);
        if (validateOrderOwner.getStatusCode().isError()) {
            return validateOrderOwner;
        }
        //不需要检测当前是否是暂停中的定制
        int result = customOrderService.resumeCustomOrder(orderCode);
        return result > 0 ? ResponseEntity.ok("修改成功") : ResponseEntity.badRequest().body("修改失败");
    }

    @PutMapping("/custom-orders/{orderCode}/delivery-time")
    @ApiOperation(value = "修改个人购买计划配送时间", response = String.class)
    public ResponseEntity deliveryTime(@PathVariable("orderCode") String orderCode,
                                       @Valid @NotBlank @RequestParam("deliveryTime") String deliveryTime,
                                       Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        ResponseEntity validateOrderOwner = validateOrderOwner(userId, orderCode);
        if (validateOrderOwner.getStatusCode().isError()) {
            return validateOrderOwner;
        }
        //修改配送时间
        CustomOrder updateCustomOrder = new CustomOrder();
        updateCustomOrder.setCustomOrderCode(orderCode);
        updateCustomOrder.setDeliveryTime(deliveryTime);
        customOrderService.updateByCode(updateCustomOrder);
        return ResponseEntity.ok("修改成功");
    }

    @PutMapping("/custom-orders/{orderCode}/extraction")
    @ApiOperation(value = "手动提取套餐", response = OrderDetailResult.class)
    public ResponseEntity extraction(@PathVariable("orderCode") String orderCode,
                                     @RequestParam("remark") String remark,
                                     @Valid @NotBlank @RequestParam("deliveryTime") String deliveryTime,
                                     Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        ResponseEntity validateOrderOwner = validateOrderOwner(userId, orderCode);
        if (validateOrderOwner.getStatusCode().isError()) {
            return validateOrderOwner;
        }
        CustomOrder customOrder = (CustomOrder) validateOrderOwner.getBody();
        if (Objects.equals(customOrder.getStatus(), CustomOrderStatus.PAUSE_DELIVERY)) {
            return ResponseEntity.badRequest().body("非定制中订单");
        }
        if (customOrder.getRemainingQty() < 1) {
            return ResponseEntity.badRequest().body("提取次数不足");
        }
        //提取定制计划
        Tips result = customOrderService.extraction(customOrder, deliveryTime, remark);
        return result.err() ? ResponseEntity.badRequest().body(result.getMessage()) : ResponseEntity.ok(result.getData());
    }

    @GetMapping("/custom-orders/count/status")
    @ApiOperation(value = "我的定制订单状态统计")
    public ResponseEntity<CustomOrderGroupCount> statusCount(Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        return ResponseEntity.ok(customOrderService.statusCount(userId).getData());
    }


    @PostMapping("/custom-orders/status")
    @ApiOperation(value = "我的订单状态列表")
    public ResponseEntity<Pages<CustomOrder>> customOrderList(@RequestBody CustomOrder customOrder, Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        customOrder.setUserId(userId);
        return ResponseEntity.ok(customOrderService.customOrderListByStatus(customOrder, true));
    }

    @GetMapping("/custom-orders/{orderCode}/detail")
    @ApiOperation(value = "个人购买定制计划-定制详情", response = CustomOrder.class)
    public ResponseEntity customOrderDetail(@PathVariable("orderCode") String orderCode, Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        ResponseEntity validateOrderOwner = validateOrderOwner(userId, orderCode);
        if (validateOrderOwner.getStatusCode().isError()) {
            return validateOrderOwner;
        }
        Tips<CustomOrder> tips = customOrderService.selectByCode(orderCode, true);
        if (tips.err()) {
            return ResponseEntity.badRequest().body(tips.getMessage());
        }
        return ResponseEntity.ok(tips.getData());
    }

    @Sessions.Uncheck
    @PostMapping("/custom-orders/pages")
    @ApiOperation(value = "根据条件分页查询定制订单信息列表(后台)", response = CustomOrder.class, responseContainer = "Set")
    @ApiHideBodyProperty({"user", "customOrderDeliveryList", "customPlan"})
    public ResponseEntity search(@RequestBody CustomOrder customOrder) {
        log.debug("根据条件分页查询定制订单信息列表\t param:{}", customOrder);

        Pages<CustomOrder> pages = customOrderService.pageList(customOrder);
        return ResponseEntity.ok(pages);
    }

    @Sessions.Uncheck
    @GetMapping("/custom-orders/{orderCode}")
    @ApiOperation(value = "根据orderCode查询定制订单信息(后台)", response = CustomOrder.class)
    @ApiImplicitParam(paramType = ApiParamType.PATH, name = "orderCode", value = "定制订单id", dataType = "String", required = true)
    @ApiHideBodyProperty({"user", "customOrderDeliveryList", "customPlan"})
    public ResponseEntity selectByCode(@PathVariable("orderCode") String orderCode) {
        log.debug("根据条件分页查询定制订单信息列表\t param:{}", orderCode);

        Tips tips = customOrderService.selectByCode(orderCode, true);
        if (tips.err()) {
            return ResponseEntity.badRequest().body(tips.getMessage());
        }
        CustomOrder customOrder = (CustomOrder) tips.getData();
        // 查找用户信息
        ResponseEntity userEntity = baseUserServerFeign.findById(customOrder.getUserId());
        if (userEntity.getStatusCode().isError() || Objects.isNull(userEntity.getBody())) {
            return ResponseEntity.badRequest().body("查找用户信息失败");
        }
        UserDetailResult userDetailResult = (UserDetailResult) userEntity.getBody();
        customOrder.setNickname(userDetailResult.getNickname());
        customOrder.setPhone(userDetailResult.getPhone());
        // 自动配送解析配送时间
        if (Objects.equals(CustomOrderBuyType.AUTO, customOrder.getDeliveryType())) {
            String deliveryTime = customOrder.getDeliveryTime();
            try {
                JSONObject jsonObject = new JSONObject(deliveryTime);
                customOrder.setDeliveryTime(jsonObject.getString("display"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return ResponseEntity.ok(customOrder);
    }

    //验证是否属于当前用户的订单
    private ResponseEntity validateOrderOwner(Long userId, String customOrderCode) {

        CustomOrder customOrder = customOrderService.selectByCode(customOrderCode);
        if (Objects.isNull(customOrder)) {
            return ResponseEntity.badRequest().body("未找到定制订单");
        }
        if (!Objects.equals(customOrder.getUserId(), userId)) {
            return ResponseEntity.badRequest().body("当前操作订单不属于登录用户");
        }
        return ResponseEntity.ok(customOrder);
    }
}
