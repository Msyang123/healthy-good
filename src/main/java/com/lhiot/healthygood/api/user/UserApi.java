package com.lhiot.healthygood.api.user;

import com.leon.microx.util.Maps;
import com.leon.microx.util.StringUtils;
import com.leon.microx.web.result.Tips;
import com.leon.microx.web.session.Authority;
import com.leon.microx.web.session.Sessions;
import com.lhiot.healthygood.domain.doctor.Achievement;
import com.lhiot.healthygood.domain.template.CaptchaTemplate;
import com.lhiot.healthygood.domain.template.FreeSignName;
import com.lhiot.healthygood.domain.user.*;
import com.lhiot.healthygood.type.ApplicationType;
import com.lhiot.healthygood.type.DateTypeEnum;
import com.lhiot.healthygood.type.PeriodType;
import com.lhiot.healthygood.feign.BaseUserServerFeign;
import com.lhiot.healthygood.feign.ThirdpartyServerFeign;
import com.lhiot.healthygood.service.doctor.DoctorAchievementLogService;
import com.lhiot.healthygood.service.user.DoctorUserService;
import com.lhiot.healthygood.service.user.FruitDoctorService;
import com.lhiot.healthygood.service.user.FruitDoctorUserService;
import com.lhiot.healthygood.wechat.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 用户信息api
 *  @author yijun
 *  @date 2018-11-20
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
    private final DoctorUserService doctorUserService;
    private final BaseUserServerFeign baseUserServerFeign;
    private final ThirdpartyServerFeign thirdpartyServerFeign;
    private final DoctorAchievementLogService doctorAchievementLogService;
    private Sessions session;
    private RedissonClient redissonClient;
    @Autowired
    public UserApi(WeChatUtil weChatUtil, FruitDoctorUserService fruitDoctorUserService,
                   FruitDoctorService fruitDoctorService,
                   DoctorUserService doctorUserService,
                   BaseUserServerFeign baseUserServerFeign,
                   ThirdpartyServerFeign thirdpartyServerFeign,
                   ObjectProvider<Sessions> sessionsObjectProvider,
                   RedissonClient redissonClient,
                   DoctorAchievementLogService doctorAchievementLogService) {
        this.weChatUtil = weChatUtil;
        this.fruitDoctorUserService = fruitDoctorUserService;
        this.fruitDoctorService = fruitDoctorService;
        this.doctorUserService = doctorUserService;
        this.baseUserServerFeign = baseUserServerFeign;
        this.thirdpartyServerFeign = thirdpartyServerFeign;
        this.doctorAchievementLogService = doctorAchievementLogService;
        this.redissonClient = redissonClient;
        this.session = sessionsObjectProvider.getIfAvailable();
    }

    /*****************************************微信授权登陆*****************************************************/
    //第一步
    @Sessions.Uncheck
    @GetMapping("/wechat/login")
    @ApiOperation(value = "微信oauth鉴权登录 获取authorize url用于前端跳转", response = String.class)
    public ResponseEntity<String> wechatLogin(@RequestParam("uri") String uri, @RequestParam("inviteCode") String inviteCode) throws UnsupportedEncodingException {
        String requestUrl = MessageFormat.format(weChatUtil.OAUTH2_URL, weChatUtil.getProperties().getWeChatOauth().getAppId(),
                URLEncoder.encode(weChatUtil.getProperties().getWeChatOauth().getAppRedirectUri(),"UTF-8"),"snsapi_userinfo",uri+"|"+inviteCode);
        return ResponseEntity.ok(requestUrl);
    }

    //通过code换取网页授权access_token
    @Sessions.Uncheck
    @GetMapping ("/wechat/authorize")
    @ApiOperation(value = "微信oauth鉴权登录 authorize back之后处理业务", response = String.class)
    public void wechatAuthorize(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String,String> resultMap= paramsToMap(request);
        String code=resultMap.get("code");
        String state=resultMap.get("state");
        String[] states=state.split("\\|");

        String inviteCode = null;
        String clientUri=states[0];
        if(states.length > 1){
            inviteCode = states[1];
        }
        log.info("===========>state:"+state);
        AccessToken accessToken=weChatUtil.getAccessTokenByCode(weChatUtil.getProperties().getWeChatOauth().getAppId(),weChatUtil.getProperties().getWeChatOauth().getAppSecret(),code);
        log.info("===========>weixinAuthorize:"+accessToken);
        //判断是否在数据库中存在此记录，如果存在直接登录，否则就注册用户微信信息
        RMapCache<String,String> cache=  redissonClient.getMapCache(PREFIX_REDIS+"userToken");
        //将access_token(2小时) 缓存起来
        cache.put("accessToken"+accessToken.getOpenId(),accessToken.getAccessToken(),2, TimeUnit.HOURS);
        //redis缓存 refresh_token一个月
        cache.put("refreshToken"+accessToken.getOpenId(),accessToken.getRefreshToken(),30, TimeUnit.DAYS);
        FruitDoctor fruitDoctor = null;
        if(StringUtils.isNotBlank(inviteCode)){
            fruitDoctor = fruitDoctorService.findDoctorByInviteCode(inviteCode);
        }
        //如果用户不存在且邀请码有效则创建用户并绑定鲜果师，否则不做操作或者绑定鲜果师
        ResponseEntity searchUserEntity= baseUserServerFeign.findByOpenId(accessToken.getOpenId());
        //注册
        if(searchUserEntity.getStatusCode().isError()){
            //写入数据库中
            String weixinUserInfo=weChatUtil.getOauth2UserInfo(accessToken.getOpenId(),accessToken.getAccessToken());
            WeChatRegisterParam weChatRegisterParam = fruitDoctorUserService.convert(weixinUserInfo);//传给基础服务的用户数据
            if(Objects.nonNull(fruitDoctor)){
                weChatRegisterParam.setDoctorId(fruitDoctor.getId());
            }
            //新增用户
            Tips tips =fruitDoctorUserService.create(weChatRegisterParam);
            UserDetailResult userDetailResult = (UserDetailResult) tips.getData();
            Sessions.User sessionUser = session.create(request).user(Maps.of("userId",userDetailResult.getId(),"openId",userDetailResult.getOpenId()))
                    .timeToLive(30, TimeUnit.MINUTES)
                    .authorities(Authority.of("/**", RequestMethod.values()));// TODO 还没有加权限;
            String sessionId = session.cache(sessionUser);
            clientUri=accessToken.getOpenId()+"?sessionId="+sessionId+"&clientUri="+clientUri;
        }else{
            UserDetailResult searchUser = (UserDetailResult) searchUserEntity.getBody();
            //判断用户是否绑定鲜果师，没有绑定则绑定,并且鲜果师不是自己
            if((Objects.isNull(searchUser.getDoctorId()) || Objects.equals(0L, searchUser.getId())) && Objects.nonNull(fruitDoctor)){
                if (!Objects.equals(fruitDoctor.getUserId(), searchUser.getId())){
                    DoctorUser doctorUser = new DoctorUser();
                    doctorUser.setDoctorId(fruitDoctor.getId());
                    doctorUser.setUserId(searchUser.getId());
                    doctorUser.setRemark(searchUser.getNickname());
                    doctorUserService.create(doctorUser);
                }
            }
            Sessions.User sessionUser = session.create(request).user(Maps.of("userId",searchUser.getId(),"openId",searchUser.getOpenId()))
                    .timeToLive(30, TimeUnit.MINUTES)
                    .authorities(Authority.of("/**", RequestMethod.values()));// TODO 还没有加权限;
            String sessionId = session.cache(sessionUser);
            clientUri=accessToken.getOpenId()+"?sessionId="+sessionId+"&clientUri="+clientUri;
        }
        log.info("用户sendRedirect:"+weChatUtil.getProperties().getWeChatOauth().getAppFrontUri()+clientUri);
        response.sendRedirect(weChatUtil.getProperties().getWeChatOauth().getAppFrontUri()+clientUri);
        return;
    }

    @GetMapping ("/session")
    @ApiOperation(value = "根据sessionId重新获取session", response = String.class)
    public ResponseEntity userInfo(HttpServletRequest request){
        String sessionId = session.id(request);
        String openId = session.user(sessionId).getUser().get("openId").toString();
        ResponseEntity searchUserEntity= baseUserServerFeign.findByOpenId(openId);
        if (searchUserEntity.getStatusCode().isError()){
            return ResponseEntity.badRequest().body(searchUserEntity.getBody());
        }
        UserDetailResult searchUser = (UserDetailResult) searchUserEntity.getBody();
        if (Objects.isNull(searchUser)){
            return ResponseEntity.badRequest().body("用户不存在！");
        }
        FruitDoctor fruitDoctor = fruitDoctorService.selectByUserId(Long.valueOf(searchUser.getId()));
        if (Objects.nonNull(fruitDoctor)){
            searchUser.setFruitDoctor(true);
            searchUser.setFruitDoctorInfo(fruitDoctor);
        }
        return ResponseEntity.ok(searchUser);
    }

    @Sessions.Uncheck
    @PostMapping ("/wechat/{openId}/session")
    @ApiImplicitParam(paramType = "path", name = "openId", value = "openId", required = true, dataType = "String")
    @ApiOperation(value = "根据openId重新获取session", response = String.class)
    public ResponseEntity session(HttpServletRequest request,@PathVariable("openId") String openId) {
        ResponseEntity searchUserEntity= baseUserServerFeign.findByOpenId(openId);
        if (searchUserEntity.getStatusCode().isError()){
            return ResponseEntity.badRequest().body(searchUserEntity.getBody());
        }
        UserDetailResult searchUser = (UserDetailResult) searchUserEntity.getBody();
        Sessions.User sessionUser = session.create(request).user(Maps.of("userId",searchUser.getId(),"openId",searchUser.getOpenId())).timeToLive(30, TimeUnit.MINUTES)
                .authorities(Authority.of("/**", RequestMethod.values()));
        String sessionId = session.cache(sessionUser);
        return ResponseEntity.ok()
                .header(Sessions.HTTP_HEADER_NAME, sessionId).body(sessionId);
    }
    @Sessions.Uncheck
    @GetMapping("/wechat/token")
    @ApiOperation(value = "微信oauth Token 全局缓存的", response = Token.class)
    public ResponseEntity<Token> token() throws IOException {
        RMapCache<String,Token> cache=  redissonClient.getMapCache(PREFIX_REDIS+"token");
        Token token=cache.get("token");
        //先从缓存中获取 如果为空再去微信服务器获取
        if(Objects.isNull(token)){
            token = weChatUtil.getToken();
            cache.put("token",token,2, TimeUnit.HOURS);//缓存2小时
        }
        return ResponseEntity.ok(token);
    }

    @Sessions.Uncheck
    @GetMapping("/wechat/ticket")
    @ApiOperation(value = "微信oauth jsapiTicket", response = JsapiTicket.class)
    public ResponseEntity<JsapiPaySign> jsapiTicket(@RequestParam("url") String url)  throws IOException {
        log.info("============================>获取jsapiTicket");
        RMapCache<String,Object> cache=  redissonClient.getMapCache(PREFIX_REDIS+"token");
        Token token=(Token)cache.get("token");
        //先从缓存中获取 如果为空再去微信服务器获取
        if(Objects.isNull(token)){
            token = weChatUtil.getToken();
            cache.put("token",token,2, TimeUnit.HOURS);//缓存2小时
        }
        //获取缓存中的js ticket (2小时) 缓存
        JsapiTicket ticket=(JsapiTicket)cache.get("ticket");
        if(Objects.isNull(ticket)){
            try{
                ticket = weChatUtil.getJsapiTicket(token.getAccessToken());
                cache.put("ticket",ticket,2, TimeUnit.HOURS);//缓存2小时
            }catch (Exception e){
                //重试
                try{
                    e.printStackTrace();
                    ticket = weChatUtil.getJsapiTicket(token.getAccessToken());
                    cache.put("ticket",ticket,2, TimeUnit.HOURS);//缓存2小时
                }catch (Exception e2){
                    e2.printStackTrace();
                    ticket = weChatUtil.getJsapiTicket(token.getAccessToken());
                    cache.put("ticket",ticket,2, TimeUnit.HOURS);//缓存2小时
                }
            }
        }

        String timestamp = Long.toString(System.currentTimeMillis() / 1000);
        String nonceStr = UUID.randomUUID().toString();
        String decodedUrl = URLDecoder.decode(url, "UTF-8");
        String signature = weChatUtil.getSignature(ticket.getTicket(), timestamp, nonceStr, decodedUrl);
        //构造jsapi返回结果
        JsapiPaySign jsapiPaySign=new JsapiPaySign();
        jsapiPaySign.setAppid(weChatUtil.getProperties().getWeChatOauth().getAppId());
        jsapiPaySign.setNonceStr(nonceStr);
        jsapiPaySign.setTimestamp(timestamp);
        jsapiPaySign.setSignature(signature);
        log.info("===============================>jsapiPaySign:"+jsapiPaySign);
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
    @ApiOperation(value = "发送申请验证码短信")
    @ApiImplicitParam(paramType = "query", name = "phone", value = "发送用户注册验证码对应手机号", required = true, dataType = "String")
    public ResponseEntity captcha(@RequestParam String phone) {
        //TODO 需要申请发送短信模板
        ResponseEntity<UserDetailResult> user = baseUserServerFeign.findByPhone(phone, ApplicationType.FRUIT_DOCTOR);
        if(user.getStatusCodeValue() > 200 && Objects.nonNull(user.getBody())){
            return ResponseEntity.badRequest().body("用户已注册");
        }
        CaptchaParam captchaParam=new CaptchaParam();
        captchaParam.setApplicationName("和色果膳商城");
        captchaParam.setPhoneNumber(phone);
        captchaParam.setFreeSignName(FreeSignName.FRUIT_DOCTOR);
        return thirdpartyServerFeign.captcha(CaptchaTemplate.REGISTER,captchaParam);
    }

    @PutMapping("/binding")
    @ApiOperation(value = "用户绑定手机号")
    @ApiImplicitParam(paramType = "body", name = "validateParam", value = "要用户注册", required = true, dataType = "ValidateParam")
    public ResponseEntity bandPhone(@ApiIgnore HttpServletRequest request, @RequestBody ValidateParam validateParam) {
        String sessionId = session.id(request);
        Long userId = (Long) session.user(sessionId).getUser().get("userId");
        UserBindingPhoneParam userBindingPhoneParam = new UserBindingPhoneParam();
        userBindingPhoneParam.setPhone(validateParam.getPhoneNumber());
        userBindingPhoneParam.setApplicationType(ApplicationType.FRUIT_DOCTOR);
        ResponseEntity userInfo = baseUserServerFeign.userBindingPhone(userId,userBindingPhoneParam);
        if (userInfo.getStatusCode().isError()){
            return ResponseEntity.badRequest().body(userInfo.getBody());
        }
        return ResponseEntity.ok(userInfo.getBody());
    }

    @Sessions.Uncheck
    @ApiOperation("统计用户总计消费数及本月消费数")
    @ApiImplicitParam(paramType = "path", name = "userId", dataType = "Long", required = true, value = "用户id")
    @GetMapping("/{userId}/consumption")
    public ResponseEntity<?> findUserAchievement(@PathVariable Long userId){
        //统计本月订单数和消费额
        Achievement thisMonth =
                doctorAchievementLogService.achievement(DateTypeEnum.MONTH, PeriodType.current, null, true, false,userId);
        //统计总订单数和消费额
        Achievement total =
                doctorAchievementLogService.achievement(DateTypeEnum.MONTH, PeriodType.current, null, true, true,userId);
        //构建返回值
        total.setSalesAmountOfThisMonth(thisMonth.getSalesAmount());
        total.setOrderCountOfThisMonth(thisMonth.getOrderCount());

        //获取用户信息
        ResponseEntity<UserDetailResult> userInfo = baseUserServerFeign.findById(userId);
        if (userInfo.getStatusCode().isError()){
            return ResponseEntity.badRequest().body(userInfo.getBody());
        }
        total.setAvatar(userInfo.getBody().getAvatar());
        total.setDescription(userInfo.getBody().getDescription());
        return ResponseEntity.ok(total);
    }
}
