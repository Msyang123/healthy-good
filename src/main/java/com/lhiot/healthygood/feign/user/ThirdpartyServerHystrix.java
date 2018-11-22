package com.lhiot.healthygood.feign.user;

import com.lhiot.healthygood.domain.template.CaptchaTemplate;
import com.lhiot.healthygood.domain.user.CaptchaParam;
import com.lhiot.healthygood.domain.user.ValidateParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 *
 * Created by yj.
 * 熔断器
 */
@Slf4j
@Component
public class ThirdpartyServerHystrix implements ThirdpartyServerFeign {


	@Override
	public ResponseEntity<String> captcha(CaptchaTemplate template, CaptchaParam parameters) {
		return null;
	}

	@Override
	public ResponseEntity<String> validate(CaptchaTemplate template, ValidateParam parameters) {
		return null;
	}
}
