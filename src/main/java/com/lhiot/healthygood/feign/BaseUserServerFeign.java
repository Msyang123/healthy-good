package com.lhiot.healthygood.feign;

import com.lhiot.healthygood.domain.user.UserBindingPhoneParam;
import com.lhiot.healthygood.feign.model.UserDetailResult;
import com.lhiot.healthygood.feign.model.WeChatRegisterParam;
import com.lhiot.healthygood.feign.type.ApplicationType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;


/**
 * 用户中心基础服务
 * Created by yj
 */
@Component
@FeignClient(value = "BASIC-USER-SERVICE-V1-0")
public interface BaseUserServerFeign {


	/**
	 * 根据id查询公共用户
	 */
    @RequestMapping(value="/users/{id}",method = RequestMethod.GET)
    ResponseEntity<UserDetailResult> findById(@PathVariable("id") Long id);

	/**
	 * 微信注册
	 */
	@RequestMapping(value="/we-chat/users",method = RequestMethod.POST)
	ResponseEntity registerByWeChat(@RequestBody WeChatRegisterParam baseUser);

	/**
	 * 根据用户OpenID查询用户信息
	 */
	@RequestMapping(value="/users/open-id/{openId}",method = RequestMethod.GET)
	ResponseEntity<UserDetailResult> findByOpenId(@PathVariable("openId") String openId);

	/**
	 * 根据业务手机号码查询用户信息
	 * @param phoneNumber
	 * @param applicationType
	 * @return
	 */
	@RequestMapping(value="/users/phone/{phoneNumber}",method = RequestMethod.GET)
	ResponseEntity<UserDetailResult> findByPhone(@PathVariable("phoneNumber") String phoneNumber, @RequestParam("applicationType") ApplicationType applicationType);

	/**
	 *用户绑定手机号码---在业务上规避一个手机号存在多个账号
	 * @param userId
	 * @param param
	 * @return
	 */
	@RequestMapping(value="/users/{id}/binding-phone",method = RequestMethod.PUT)
	ResponseEntity userBindingPhone(@PathVariable("id") Long userId,@RequestBody UserBindingPhoneParam param);
}
