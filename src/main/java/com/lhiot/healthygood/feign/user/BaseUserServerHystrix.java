package com.lhiot.healthygood.feign.user;

import com.lhiot.healthygood.domain.user.UserDetailResult;
import com.lhiot.healthygood.domain.user.WeChatRegisterParam;
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
public class BaseUserServerHystrix implements BaseUserServerFeign {

	@Override
	public ResponseEntity<UserDetailResult> findById(Long id) {
		log.info("根据id查询公共用户错误:"+id);
		return null;
	}

	@Override
	public ResponseEntity registerByWeChat(WeChatRegisterParam baseUser) {
		return null;
	}

	@Override
	public ResponseEntity<UserDetailResult> findByOpenId(String id) {
		return null;
	}
}
