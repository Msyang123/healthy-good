package com.lhiot.healthygood.api.customplan;

import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.leon.microx.web.result.Tuple;
import com.leon.microx.web.session.Sessions;
import com.leon.microx.web.swagger.ApiHideBodyProperty;
import com.leon.microx.web.swagger.ApiParamType;
import com.lhiot.healthygood.config.HealthyGoodConfig;
import com.lhiot.healthygood.domain.customplan.CustomOrder;
import com.lhiot.healthygood.domain.customplan.CustomOrderDelivery;
import com.lhiot.healthygood.domain.customplan.CustomOrderPause;
import com.lhiot.healthygood.feign.BaseUserServerFeign;
import com.lhiot.healthygood.feign.PaymentServiceFeign;
import com.lhiot.healthygood.feign.model.PaySign;
import com.lhiot.healthygood.feign.model.UserDetailResult;
import com.lhiot.healthygood.feign.type.ApplicationType;
import com.lhiot.healthygood.feign.type.SourceType;
import com.lhiot.healthygood.service.customplan.CustomOrderService;
import com.lhiot.healthygood.type.CustomOrderStatus;
import com.lhiot.healthygood.util.FeginResponseTools;
import com.lhiot.healthygood.util.RealClientIp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
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
    @ApiOperation(value = "立即定制-创建定制计划")
    @ApiHideBodyProperty({"id", "beginCreateAt", "endCreateAt", "user", "rows", "page"})
    public ResponseEntity create(@Valid @RequestBody CustomOrder customOrder, Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        customOrder.setUserId(userId);
        Tips result = customOrderService.createCustomOrder(customOrder);
        if (result.err()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/custom-orders/{orderCode}")
    @ApiOperation(value = "取消购买定制计划")
    public ResponseEntity<Tips> cancel(@PathVariable("orderCode") String orderCode, Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        ResponseEntity<Tips> validateOrderOwner = validateOrderOwner(userId, orderCode);
        if (validateOrderOwner.getStatusCode().isError()) {
            return validateOrderOwner;
        }
        CustomOrder customOrder = (CustomOrder) validateOrderOwner.getBody().getData();
        if (!Objects.equals(customOrder.getStatus(), CustomOrderStatus.WAIT_PAYMENT))
            return ResponseEntity.badRequest().body(Tips.warn("当前定制计划非待支付状态，不能取消"));

        CustomOrder updateCustomOrder = new CustomOrder();
        updateCustomOrder.setStatus(CustomOrderStatus.INVALID);
        updateCustomOrder.setCustomOrderCode(orderCode);
        int result = customOrderService.updateByCode(updateCustomOrder, null);
        return result > 0 ? ResponseEntity.ok(Tips.info("取消成功")) : ResponseEntity.badRequest().body(Tips.warn("取消失败"));
    }

    @PostMapping("/custom-orders/{orderCode}/payment-sign")
    @ApiOperation(value = "定制计划支付时微信签名")
    public ResponseEntity paymentSign(@PathVariable("orderCode") String orderCode, HttpServletRequest request, Sessions.User user) {
        String openId = user.getUser().get("openId").toString();
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        ResponseEntity<Tips> validateOrderOwner = validateOrderOwner(userId, orderCode);
        if (validateOrderOwner.getStatusCode().isError()) {
            return validateOrderOwner;
        }
        CustomOrder customOrder = (CustomOrder) validateOrderOwner.getBody().getData();
        PaySign paySign = new PaySign();
        paySign.setApplicationType(ApplicationType.HEALTH_GOOD);
        paySign.setBackUrl(wechatPayConfig.getActivityCallbackUrl());
        paySign.setClientIp(RealClientIp.getRealIp(request));//获取客户端真实ip
        paySign.setConfigName(wechatPayConfig.getConfigName());//微信支付简称
        paySign.setFee(customOrder.getPrice());
        paySign.setMemo("定制订单支付");
        paySign.setOpenid(openId);
        paySign.setSourceType(SourceType.ACTIVITY);
        paySign.setUserId(userId);
        paySign.setAttach(orderCode);//定制订单code
        Tips<String> wxSignResponse = FeginResponseTools.convertResponse(paymentServiceFeign.wxSign(paySign));
        if (wxSignResponse.err()) {
            return ResponseEntity.badRequest().body(wxSignResponse);
        }
        return ResponseEntity.ok(wxSignResponse);
    }


    @PutMapping("/custom-orders/{orderCode}/delivery-suspension")
    @ApiOperation(value = "修改个人购买计划状态（暂停配送）")
    public ResponseEntity<Tips> deliverySuspension(@PathVariable("orderCode") String orderCode, @Valid @RequestBody CustomOrderPause customOrderPause, Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        ResponseEntity<Tips> validateOrderOwner = validateOrderOwner(userId, orderCode);
        if (validateOrderOwner.getStatusCode().isError()) {
            return validateOrderOwner;
        }
        CustomOrder customOrder = (CustomOrder) validateOrderOwner.getBody().getData();
        if (Objects.equals(customOrder.getStatus(), CustomOrderStatus.CUSTOMING)) {
            return ResponseEntity.badRequest().body(Tips.warn("非定制中订单"));
        }

        CustomOrder updateCustomOrder = new CustomOrder();
        updateCustomOrder.setCustomOrderCode(orderCode);
        updateCustomOrder.setStatus(CustomOrderStatus.PAUSE_DELIVERY);
        int result = customOrderService.updateByCode(updateCustomOrder, customOrderPause);
        return result > 0 ? ResponseEntity.ok(Tips.info("修改成功")) : ResponseEntity.badRequest().body(Tips.warn("暂停配送失败"));
    }


    @PutMapping("/custom-orders/{orderCode}/delivery-recovery")
    @ApiOperation(value = "修改个人购买计划状态（恢复配送）")
    public ResponseEntity<Tips> deliveryRecovery(@PathVariable("orderCode") String orderCode, Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        ResponseEntity<Tips> validateOrderOwner = validateOrderOwner(userId, orderCode);
        if (validateOrderOwner.getStatusCode().isError()) {
            return validateOrderOwner;
        }
        CustomOrder customOrder = (CustomOrder) validateOrderOwner.getBody().getData();
        if (Objects.equals(customOrder.getStatus(), CustomOrderStatus.PAUSE_DELIVERY)) {
            return ResponseEntity.badRequest().body(Tips.warn("非定制中订单"));
        }

        CustomOrder updateCustomOrder = new CustomOrder();
        updateCustomOrder.setCustomOrderCode(orderCode);
        updateCustomOrder.setStatus(CustomOrderStatus.CUSTOMING);
        customOrderService.updateByCode(updateCustomOrder, null);
        return ResponseEntity.ok(Tips.info("修改成功"));
    }

    @PutMapping("/custom-orders/{orderCode}/delivery-time")
    @ApiOperation(value = "修改个人购买计划配送时间")
    public ResponseEntity<Tips> deliveryTime(@PathVariable("orderCode") String orderCode,
                                             @Valid @NotBlank @RequestParam("deliveryTime") String deliveryTime, Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        ResponseEntity<Tips> validateOrderOwner = validateOrderOwner(userId, orderCode);
        if (validateOrderOwner.getStatusCode().isError()) {
            return validateOrderOwner;
        }
        //修改配送时间
        CustomOrder updateCustomOrder = new CustomOrder();
        updateCustomOrder.setCustomOrderCode(orderCode);
        updateCustomOrder.setDeliveryTime(deliveryTime);
        customOrderService.updateByCode(updateCustomOrder, null);
        return ResponseEntity.ok(Tips.info("修改成功"));
    }

    @PostMapping("/custom-orders/{orderCode}/extraction")
    @ApiOperation(value = "手动提取套餐")
    public ResponseEntity<Tips> extraction(@PathVariable("orderCode") String orderCode, @RequestParam("remark") String remark, Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        ResponseEntity<Tips> validateOrderOwner = validateOrderOwner(userId, orderCode);
        if (validateOrderOwner.getStatusCode().isError()) {
            return validateOrderOwner;
        }
        CustomOrder customOrder = (CustomOrder) validateOrderOwner.getBody().getData();
        if (Objects.equals(customOrder.getStatus(), CustomOrderStatus.PAUSE_DELIVERY)) {
            return ResponseEntity.badRequest().body(Tips.warn("非定制中订单"));
        }
        if (customOrder.getRemainingQty() < 1) {
            return ResponseEntity.badRequest().body(Tips.warn("提取次数不足"));
        }
        //提取定制计划
        Tips result = customOrderService.extraction(customOrder, remark);
        return result.err() ? ResponseEntity.badRequest().body(result) : ResponseEntity.ok(result);
    }

    @GetMapping("/custom-orders/count/status")
    @ApiOperation(value = "我的定制订单状态统计")
    public ResponseEntity<Tips> statusCount(Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        return ResponseEntity.ok(customOrderService.statusCount(userId));
    }


    @GetMapping("/custom-orders/status/{status}")
    @ApiOperation(value = "我的订单状态列表")
    public ResponseEntity<Tuple<CustomOrder>> customOrderList(@PathVariable("status") CustomOrderStatus status, Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        return ResponseEntity.ok(customOrderService.customOrderListByStatus(userId, status, true));
    }

    @GetMapping("/custom-orders/{orderCode}/detail")
    @ApiOperation(value = "个人购买定制计划-定制详情")
    public ResponseEntity<Tips> customOrderDetail(@PathVariable("orderCode") String orderCode, Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        ResponseEntity<Tips> validateOrderOwner = validateOrderOwner(userId, orderCode);
        if (validateOrderOwner.getStatusCode().isError()) {
            return validateOrderOwner;
        }
        Tips<CustomOrder> tips = customOrderService.selectByCode(orderCode, true);
        if (tips.err()) {
            return ResponseEntity.badRequest().body(Tips.warn(tips.getMessage()));
        }
        Tips<CustomOrder> customOrderTips = new Tips();
        customOrderTips.setData(tips.getData());
        return ResponseEntity.ok(customOrderTips);
    }

    //验证是否属于当前用户的订单
    private ResponseEntity<Tips> validateOrderOwner(Long userId, String customOrderCode) {

        CustomOrder customOrder = customOrderService.selectByCode(customOrderCode);
        if (Objects.isNull(customOrder)) {
            return ResponseEntity.badRequest().body(Tips.warn("未找到定制订单"));
        }
        if (!Objects.equals(customOrder.getUserId(), userId)) {
            return ResponseEntity.badRequest().body(Tips.warn("当前操作订单不属于登录用户"));
        }
        Tips tips = new Tips();
        tips.setData(customOrder);
        return ResponseEntity.ok(tips);
    }

    @Sessions.Uncheck
    @PostMapping("/custom-orders/pages")
    @ApiOperation(value = "根据条件分页查询定制订单信息列表(后台)")
    @ApiHideBodyProperty({"user","customOrderDeliveryList","customPlan"})
    public ResponseEntity<Tips> search(@RequestBody CustomOrder customOrder) {
        log.debug("根据条件分页查询定制订单信息列表\t param:{}", customOrder);

        Pages<CustomOrder> pages = customOrderService.pageList(customOrder);
        Tips<Pages<CustomOrder>> tips = new Tips();
        tips.setData(pages);
        return ResponseEntity.ok(tips);
    }

    @Sessions.Uncheck
    @GetMapping("/custom-orders/{orderCode}")
    @ApiOperation(value = "根据id查询定制订单信息(后台)")
    @ApiImplicitParam(paramType = ApiParamType.PATH, name = "orderCode", value = "定制订单id", dataType = "String", required = true)
    @ApiHideBodyProperty({"user","customOrderDeliveryList","customPlan"})
    public ResponseEntity<Tips> search(@PathVariable("orderCode") String orderCode) {
        log.debug("根据条件分页查询定制订单信息列表\t param:{}", orderCode);

        Tips<CustomOrder> tips = customOrderService.selectByCode(orderCode, true);
        if (tips.err()) {
            return ResponseEntity.badRequest().body(Tips.warn(tips.getMessage()));
        }
        CustomOrder customOrder = tips.getData();

        // 查找用户信息
        ResponseEntity<UserDetailResult> userEntity = baseUserServerFeign.findById(customOrder.getUserId());
        if (userEntity.getStatusCode().isError()) {
            return ResponseEntity.badRequest().body(Tips.warn("查找用户信息失败"));
        }
        UserDetailResult userDetailResult = userEntity.getBody();
        customOrder.setNickname(userDetailResult.getNickname());
        customOrder.setPhone(userDetailResult.getPhone());

        Tips<CustomOrder> customOrderTips = new Tips();
        customOrderTips.setData(customOrder);
        return ResponseEntity.ok(customOrderTips);
    }

}
