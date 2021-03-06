package com.lhiot.healthygood.api.user;

import com.leon.microx.util.Beans;
import com.leon.microx.util.Maps;
import com.leon.microx.util.StringUtils;
import com.leon.microx.web.result.Tips;
import com.leon.microx.web.session.Authority;
import com.leon.microx.web.session.Sessions;
import com.lhiot.healthygood.config.HealthyGoodConfig;
import com.lhiot.healthygood.domain.doctor.Achievement;
import com.lhiot.healthygood.domain.good.PagesParam;
import com.lhiot.healthygood.domain.user.DoctorCustomer;
import com.lhiot.healthygood.domain.user.FruitDoctor;
import com.lhiot.healthygood.domain.user.UserBindingPhoneParam;
import com.lhiot.healthygood.domain.user.ValidateParam;
import com.lhiot.healthygood.feign.BaseUserServerFeign;
import com.lhiot.healthygood.feign.ImsServiceFeign;
import com.lhiot.healthygood.feign.ThirdpartyServerFeign;
import com.lhiot.healthygood.feign.model.*;
import com.lhiot.healthygood.feign.type.ApplicationType;
import com.lhiot.healthygood.mq.HealthyGoodQueue;
import com.lhiot.healthygood.service.doctor.DoctorAchievementLogService;
import com.lhiot.healthygood.service.user.DoctorCustomerService;
import com.lhiot.healthygood.service.user.FruitDoctorService;
import com.lhiot.healthygood.service.user.FruitDoctorUserService;
import com.lhiot.healthygood.type.CaptchaTemplate;
import com.lhiot.healthygood.type.DateTypeEnum;
import com.lhiot.healthygood.type.FreeSignName;
import com.lhiot.healthygood.type.PeriodType;
import com.lhiot.healthygood.wechat.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户信息api
 *
 * @author yijun
 * @date 2018-11-20
 */
@Api(description = "用户信息接口")
@Slf4j
@RestController
@RequestMapping("/users")
public class UserApi {
    private final static String PREFIX_REDIS = "lhiot:healthy_good:user_";//
    private final WeChatUtil weChatUtil;
    private final FruitDoctorUserService fruitDoctorUserService;
    private final FruitDoctorService fruitDoctorService;
    private final DoctorCustomerService doctorCustomerService;
    private final BaseUserServerFeign baseUserServerFeign;
    private final ThirdpartyServerFeign thirdpartyServerFeign;
    private final DoctorAchievementLogService doctorAchievementLogService;
    private Sessions session;
    private RedissonClient redissonClient;
    private final HealthyGoodConfig.WechatOauthConfig wechatOauth;
    private final ImsServiceFeign imsServiceFeign;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public UserApi(WeChatUtil weChatUtil, FruitDoctorUserService fruitDoctorUserService,
                   FruitDoctorService fruitDoctorService,
                   DoctorCustomerService doctorCustomerService, BaseUserServerFeign baseUserServerFeign,
                   ThirdpartyServerFeign thirdpartyServerFeign,
                   ObjectProvider<Sessions> sessionsObjectProvider,
                   RedissonClient redissonClient,
                   DoctorAchievementLogService doctorAchievementLogService,
                   HealthyGoodConfig healthyGoodConfig, ImsServiceFeign imsServiceFeign, RabbitTemplate rabbitTemplate) {
        this.weChatUtil = weChatUtil;
        this.fruitDoctorUserService = fruitDoctorUserService;
        this.fruitDoctorService = fruitDoctorService;
        this.doctorCustomerService = doctorCustomerService;
        this.baseUserServerFeign = baseUserServerFeign;
        this.thirdpartyServerFeign = thirdpartyServerFeign;
        this.doctorAchievementLogService = doctorAchievementLogService;
        this.redissonClient = redissonClient;
        this.session = sessionsObjectProvider.getIfAvailable();
        this.wechatOauth = healthyGoodConfig.getWechatOauth();
        this.imsServiceFeign = imsServiceFeign;
        this.rabbitTemplate = rabbitTemplate;
    }

    /*****************************************微信授权登陆*****************************************************/
    //第一步
    @Sessions.Uncheck
    @GetMapping("/wechat/login")
    @ApiOperation(value = "微信oauth鉴权登录 获取authorize url用于前端跳转", response = String.class)
    public ResponseEntity<String> wechatLogin(@RequestParam("uri") String uri, @RequestParam("inviteCode") String inviteCode) throws UnsupportedEncodingException {
        String requestUrl = MessageFormat.format(weChatUtil.OAUTH2_URL, wechatOauth.getAppId(),
                URLEncoder.encode(wechatOauth.getAppRedirectUri(), "UTF-8"), "snsapi_userinfo", uri + "|" + inviteCode);
        return ResponseEntity.ok(requestUrl);
    }

    //通过code换取网页授权access_token
    @Sessions.Uncheck
    @GetMapping("/wechat/authorize")
    @ApiOperation(value = "微信oauth鉴权登录 authorize back之后处理业务", response = String.class)
    public void wechatAuthorize(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String> resultMap = paramsToMap(request);
        String code = (Objects.nonNull(resultMap.get("code")))?resultMap.get("code"):"#";
        String state = resultMap.get("state");
        String[] states = state.split("\\|");

        String inviteCode = null;
        String clientUri = states[0];
        if (states.length > 1) {
            inviteCode = states[1];
        }
        AccessToken accessToken = weChatUtil.getAccessTokenByCode(wechatOauth.getAppId(), wechatOauth.getAppSecret(), code);
        FruitDoctor fruitDoctor = null;
        if (StringUtils.isNotBlank(inviteCode)) {
            fruitDoctor = fruitDoctorService.findDoctorByInviteCode(inviteCode);
        }
        //如果用户不存在且邀请码有效则创建用户并绑定鲜果师，否则不做操作或者绑定鲜果师
        ResponseEntity searchUserEntity = baseUserServerFeign.findByOpenId(accessToken.getOpenId());
        if (searchUserEntity.getStatusCode().isError()) {
            String weixinUserInfo = weChatUtil.getOauth2UserInfo(accessToken.getOpenId(), accessToken.getAccessToken());
            WeChatRegisterParam weChatRegisterParam = fruitDoctorUserService.convert(weixinUserInfo);//传给基础服务的用户数据
            weChatRegisterParam.setApplicationType(ApplicationType.HEALTH_GOOD);
            //新增用户
            Tips tips = fruitDoctorUserService.create(weChatRegisterParam);
            if (tips.err()){
                return;
            }
            UserDetailResult userDetailResult = (UserDetailResult) tips.getData();
            DoctorCustomer doctorCustomerParam = new DoctorCustomer();
            if ( Objects.nonNull(fruitDoctor)) {

                doctorCustomerParam.setDoctorId(fruitDoctor.getId());
            }
            doctorCustomerParam.setUserId(userDetailResult.getId());
            doctorCustomerParam.setRemark(userDetailResult.getNickname());
            doctorCustomerParam.setOpenId(userDetailResult.getOpenId());
            clientUri = doctorCustomerService.createRelations(request,doctorCustomerParam,clientUri);//绑定鲜果师
        } else {
            UserDetailResult searchUser = (UserDetailResult) searchUserEntity.getBody();
            //判断用户是否绑定鲜果师，没有绑定则绑定,并且鲜果师不是自己
            DoctorCustomer doctorCustomerParam = new DoctorCustomer();
            if ( Objects.nonNull(fruitDoctor)) {
                if (!Objects.equals(fruitDoctor.getUserId(), searchUser.getId())) {
                    doctorCustomerParam.setDoctorId(fruitDoctor.getId());
                }
            }
            doctorCustomerParam.setUserId(searchUser.getId());
            doctorCustomerParam.setRemark(searchUser.getNickname());
            doctorCustomerParam.setOpenId(searchUser.getOpenId());
            clientUri = doctorCustomerService.createRelations(request,doctorCustomerParam,clientUri);//绑定鲜果师
        }
        log.info("用户sendRedirect:" + wechatOauth.getAppFrontUri() + clientUri);
        response.sendRedirect(wechatOauth.getAppFrontUri() + clientUri);
        return;
    }


    @GetMapping("/session")
    @ApiOperation(value = "根据sessionId重新用户信息*", response = UserDetailResult.class)
    public ResponseEntity userInfo(Sessions.User user) {
        String openId = user.getUser().get("openId").toString();
        ResponseEntity searchUserEntity = baseUserServerFeign.findByOpenId(openId);
        if (Objects.isNull(searchUserEntity) || searchUserEntity.getStatusCode().isError()) {
            return searchUserEntity;
        }
        UserDetailResult searchUser = (UserDetailResult) searchUserEntity.getBody();
        if (Objects.isNull(searchUser)) {
            return ResponseEntity.badRequest().body("用户不存在！");
        }
        FruitDoctor fruitDoctor = fruitDoctorService.selectByUserId(Long.valueOf(searchUser.getId()));
        if (Objects.nonNull(fruitDoctor)) {
            searchUser.setFruitDoctor(true);
            searchUser.setFruitDoctorInfo(fruitDoctor);
        }
        return ResponseEntity.ok(searchUser);
    }

    @Sessions.Uncheck
    @PostMapping("/wechat/{openId}/session")
    @ApiImplicitParam(paramType = "path", name = "openId", value = "openId", required = true, dataType = "String")
    @ApiOperation(value = "根据openId重新获取session", response = String.class)
    public ResponseEntity session(HttpServletRequest request, @PathVariable("openId") String openId) {
        ResponseEntity searchUserEntity = baseUserServerFeign.findByOpenId(openId);
        if (Objects.isNull(searchUserEntity) || searchUserEntity.getStatusCode().isError()) {
            return searchUserEntity;
        }
        UserDetailResult searchUser = (UserDetailResult) searchUserEntity.getBody();
        Sessions.User sessionUser = session.create(request).user(Maps.of("userId", searchUser.getId(), "baseUserId",searchUser.getBaseUserId(),
                "openId", searchUser.getOpenId())).timeToLive(30, TimeUnit.MINUTES);
        //sessionUser.authorities(Authority.of("/**", RequestMethod.values()));
        ResponseEntity imsOperationRes = imsServiceFeign.selectAuthority();
        if (imsOperationRes.getStatusCode().isError()){
            return ResponseEntity.badRequest().body("请求失败");
        }
        List<ImsOperation> imsOperations = (List<ImsOperation>) imsOperationRes.getBody();
        List<Authority> authorityList = imsOperations.stream()
                .map(op -> Authority.of(op.getAntUrl(), StringUtils.tokenizeToStringArray(op.getType(), ",")))
                .collect(Collectors.toList());
        sessionUser.authorities(authorityList);
        String sessionId = session.cache(sessionUser);
     /*   //刷新access_token 判断access_token 是否过期
        HealthyGoodQueue.DelayQueue.CANCEL_CUSTOM_ORDER.send(rabbitTemplate, searchUser.getOpenId(), 7100 * 1000);*/
        return ResponseEntity.ok()
                .header(Sessions.HTTP_HEADER_NAME, sessionId).body(sessionId);
    }

    @Sessions.Uncheck
    @GetMapping("/wechat/token")
    @ApiOperation(value = "微信oauth Token 全局缓存的")
    public ResponseEntity<Token> token() {
        RMapCache<String, Token> cache = redissonClient.getMapCache(PREFIX_REDIS + "token");
        Token token = cache.get("token");
        //先从缓存中获取 如果为空再去微信服务器获取
        if (Objects.isNull(token)) {
            token = weChatUtil.getToken();
            cache.put("token", token, 2, TimeUnit.HOURS);//缓存2小时
        }
        return ResponseEntity.ok(token);
    }

    @Sessions.Uncheck
    @GetMapping("/wechat/ticket")
    @ApiOperation(value = "微信oauth jsapiTicket")
    public ResponseEntity<JsapiPaySign> jsapiTicket(@RequestParam("url") String url) throws IOException {
        log.info("============================>获取jsapiTicket");
        RMapCache<String, Object> cache = redissonClient.getMapCache(PREFIX_REDIS + "token");
        Token token = (Token) cache.get("token");
        //先从缓存中获取 如果为空再去微信服务器获取
        if (Objects.isNull(token)) {
            token = weChatUtil.getToken();
            cache.put("token", token, 2, TimeUnit.HOURS);//缓存2小时
        }
        //获取缓存中的js ticket (2小时) 缓存
        JsapiTicket ticket = (JsapiTicket) cache.get("ticket");
        if (Objects.isNull(ticket)) {
            try {
                ticket = weChatUtil.getJsapiTicket(token.getAccessToken());
                cache.put("ticket", ticket, 2, TimeUnit.HOURS);//缓存2小时
            } catch (Exception e) {
                //重试
                try {
                    e.printStackTrace();
                    ticket = weChatUtil.getJsapiTicket(token.getAccessToken());
                    cache.put("ticket", ticket, 2, TimeUnit.HOURS);//缓存2小时
                } catch (Exception e2) {
                    e2.printStackTrace();
                    ticket = weChatUtil.getJsapiTicket(token.getAccessToken());
                    cache.put("ticket", ticket, 2, TimeUnit.HOURS);//缓存2小时
                }
            }
        }

        String timestamp = Long.toString(System.currentTimeMillis() / 1000);
        String nonceStr = UUID.randomUUID().toString();
        String decodedUrl = URLDecoder.decode(url, "UTF-8");
        String signature = weChatUtil.getSignature(ticket.getTicket(), timestamp, nonceStr, decodedUrl);
        //构造jsapi返回结果
        JsapiPaySign jsapiPaySign = new JsapiPaySign();
        jsapiPaySign.setAppid(wechatOauth.getAppId());
        jsapiPaySign.setNonceStr(nonceStr);
        jsapiPaySign.setTimestamp(timestamp);
        jsapiPaySign.setSignature(signature);
        log.info("===============================>jsapiPaySign:" + jsapiPaySign);
        return ResponseEntity.ok(jsapiPaySign);
    }

    private Map<String, String> paramsToMap(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((name, values) -> {
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                if (i == values.length - 1) {
                    valueStr = valueStr + values[i];
                } else {
                    valueStr = valueStr + values[i] + ",";
                }
            }
            params.put(name, valueStr);
        });
        return params;
    }

    @PostMapping("/sms/captcha")
    @ApiOperation(value = "发送申请验证码短信*")
    @ApiImplicitParam(paramType = "query", name = "phone", value = "发送用户注册验证码对应手机号", required = true, dataType = "String")
    public ResponseEntity captcha(Sessions.User user, @RequestParam String phone) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());

        ResponseEntity userDetailResultResponseEntity = baseUserServerFeign.findById(userId);
        if (Objects.isNull(userDetailResultResponseEntity) || userDetailResultResponseEntity.getStatusCode().isError()) {
            return userDetailResultResponseEntity;
        }
        ResponseEntity presentUserRes = baseUserServerFeign.findByPhone(phone,ApplicationType.HEALTH_GOOD);
        if (presentUserRes.getStatusCode().is2xxSuccessful() && Objects.nonNull(presentUserRes)){
            return ResponseEntity.badRequest().body("手机号码已存在");
        }
        CaptchaParam captchaParam = new CaptchaParam();
        captchaParam.setApplicationName("和色果膳商城");
        captchaParam.setPhoneNumber(phone);
        captchaParam.setFreeSignName(FreeSignName.FRUIT_DOCTOR);
        return thirdpartyServerFeign.captcha(CaptchaTemplate.REGISTER, captchaParam);
    }

    @PutMapping("/binding")
    @ApiOperation(value = "用户绑定手机号*",response = UserDetailResult.class)
    @ApiImplicitParam(paramType = "body", name = "validateParam", value = "要用户注册", required = true, dataType = "ValidateParam")
    public ResponseEntity bandPhone(@ApiIgnore Sessions.User user, @RequestBody ValidateParam validateParam) {
        Long userId = (Long) user.getUser().get("userId");
        ResponseEntity responseEntity = baseUserServerFeign.findById(userId);
        if (responseEntity.getStatusCode().isError()){
            return ResponseEntity.badRequest().body(responseEntity.getBody());
        }
        UserBindingPhoneParam userBindingPhoneParam = new UserBindingPhoneParam();
        userBindingPhoneParam.setPhone(validateParam.getPhoneNumber());
        userBindingPhoneParam.setApplicationType(ApplicationType.HEALTH_GOOD);
        return baseUserServerFeign.userBindingPhone(userId, userBindingPhoneParam);
    }

    @Sessions.Uncheck
    @ApiOperation(value = "统计用户总计消费数及本月消费数", response = Achievement.class)
    @ApiImplicitParam(paramType = "path", name = "userId", dataType = "Long", required = true, value = "用户id")
    @GetMapping("/{userId}/consumption")
    public ResponseEntity findUserAchievement(@PathVariable Long userId) {
        //统计本月订单数和消费额
        Achievement thisMonth =
                doctorAchievementLogService.achievement(DateTypeEnum.MONTH, PeriodType.current, null, true, false, userId);
        //统计总订单数和消费额
        Achievement total =
                doctorAchievementLogService.achievement(DateTypeEnum.MONTH, PeriodType.current, null, true, true, userId);
        //构建返回值
        total.setSalesAmountOfThisMonth(thisMonth.getSalesAmount());
        total.setOrderCountOfThisMonth(thisMonth.getOrderCount());

        //获取用户信息
        ResponseEntity userDetailResult = baseUserServerFeign.findById(userId);
        if (Objects.isNull(userDetailResult) || userDetailResult.getStatusCode().isError()) {
            return userDetailResult;
        }
        UserDetailResult users = (UserDetailResult) userDetailResult.getBody();
        FruitDoctor doctor = fruitDoctorService.selectByUserId(Long.valueOf(users.getId()));
        if (Objects.nonNull(doctor)) {
            users.setFruitDoctor(true);
            users.setFruitDoctorInfo(doctor);
        }
        DoctorCustomer doctorCustomer = doctorCustomerService.selectByUserId(userId);
        users.setRemark(doctorCustomer.getRemark());
        total.setUserDetailResult(users);
        return ResponseEntity.ok(total);
    }

    @ApiOperation(value = "用户余额明细*",response = BalanceLog.class)
    @PostMapping("/balance-log/page")
    public ResponseEntity findUserBalanceLog(Sessions.User user,@RequestBody PagesParam pagesParam){
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        ResponseEntity userDetailResult = baseUserServerFeign.findById(userId);
        if (Objects.isNull(userDetailResult.getBody()) || userDetailResult.getStatusCode().isError()) {
            return userDetailResult;
        }
        UserDetailResult users = (UserDetailResult) userDetailResult.getBody();
        if (Objects.isNull(users.getBaseUserId())){
            return ResponseEntity.ok().build();
        }
        BalanceLogParam balanceLogParam = new BalanceLogParam();
        //BeanUtils.copyProperties(pagesParam,balanceLogParam);
        Beans.from(pagesParam).to(balanceLogParam);
        balanceLogParam.setBaseUserId(users.getBaseUserId());
        balanceLogParam.setApplicationType(ApplicationType.HEALTH_GOOD);
        ResponseEntity responseEntity =  baseUserServerFeign.searchBalanceLog(balanceLogParam);
        return responseEntity.getStatusCode().is2xxSuccessful() ? ResponseEntity.ok(responseEntity.getBody()) : ResponseEntity.badRequest().body(responseEntity.getBody());
    }
}
