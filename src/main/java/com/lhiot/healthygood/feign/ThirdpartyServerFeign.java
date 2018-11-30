package com.lhiot.healthygood.feign;

import com.lhiot.healthygood.domain.template.CaptchaTemplate;
import com.lhiot.healthygood.domain.user.CaptchaParam;
import com.lhiot.healthygood.domain.user.ValidateParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


/**
 * 第三方服务
 * Created by yj
 */
@Component
@FeignClient(value = "THIRDPARTY-SERVICE-V1-0")
public interface ThirdpartyServerFeign {


	/**
	 * 发送短信
	 */
    @RequestMapping(value="/sms-captcha/{template}",method = RequestMethod.POST)
	ResponseEntity<String> captcha(@PathVariable("template") CaptchaTemplate template, @RequestBody CaptchaParam parameters);
    

    /**
	 * 验证短信
	 * @return
	 */
	@RequestMapping(value = "/sms-captcha/{template}/validate", method = RequestMethod.POST)
    ResponseEntity<String> validate(@PathVariable("template") CaptchaTemplate template, @RequestBody ValidateParam parameters);
}
