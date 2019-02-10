package com.lhiot.healthygood.feign;

import com.lhiot.healthygood.domain.user.ValidateParam;
import com.lhiot.healthygood.feign.model.AccountAuditParam;
import com.lhiot.healthygood.feign.model.CaptchaParam;
import com.lhiot.healthygood.feign.model.DeliveryParam;
import com.lhiot.healthygood.feign.model.HdOrderInfo;
import com.lhiot.healthygood.type.CaptchaTemplate;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;


/**
 * 第三方服务
 * Created by yj
 */
@Component
@FeignClient(value = "THIRDPARTY-SERVICE-V1-0")
public interface ThirdpartyServerFeign {

    /**
     * 短信通知 - 账号审核
     */


    @RequestMapping(value = "/sms-notification/account-audit", method = RequestMethod.POST)
    ResponseEntity accountAudit(@RequestBody AccountAuditParam parameters);

    /**
     * 短信通知 - 提货通知
     *
     * @param parameters
     * @return
     */
    @RequestMapping(value = "/sms-notification/delivery", method = RequestMethod.POST)
    ResponseEntity delivery(@RequestBody DeliveryParam parameters);

    /**
     * 发送验证短信
     *
     * @param template
     * @param parameters
     * @return
     */
    @RequestMapping(value = "/sms-captcha/{template}", method = RequestMethod.POST)
    ResponseEntity<String> captcha(@PathVariable("template") CaptchaTemplate template, @RequestBody CaptchaParam parameters);

    /**
     * 校验短信验证码
     *
     * @param template
     * @param parameters
     * @return
     */
    @RequestMapping(value = "/sms-captcha/{template}/validate", method = RequestMethod.POST)
    ResponseEntity validate(@PathVariable("template") CaptchaTemplate template, @RequestBody ValidateParam parameters);


    /**
     * 门店减库存 已废弃
     * @see OrderServiceFeign sendOrderToHd()
     * @param orderInfo
     * @return
     */
    @Deprecated
    @RequestMapping(value = "/hd/inventory", method = RequestMethod.PUT)
    ResponseEntity<String> reduce(@RequestBody HdOrderInfo orderInfo);

    /**
     * 门店查库存
     * @param storeCode
     * @param skuIds
     * @return
     */
    @RequestMapping(value = "/hd/sku/{storeCode}", method = RequestMethod.POST)
    ResponseEntity<Map<String, Object>> querySku(@PathVariable("storeCode") String storeCode, @RequestBody String[] skuIds);

}
