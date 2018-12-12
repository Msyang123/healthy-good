package com.lhiot.healthygood.service.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leon.microx.util.StringUtils;
import com.leon.microx.web.result.Tips;
import com.lhiot.dc.dictionary.DictionaryClient;
import com.lhiot.dc.dictionary.module.Dictionary;
import com.lhiot.healthygood.domain.user.DoctorCustomer;
import com.lhiot.healthygood.event.SendCaptchaSmsEvent;
import com.lhiot.healthygood.feign.BaseUserServerFeign;
import com.lhiot.healthygood.feign.model.UserDetailResult;
import com.lhiot.healthygood.feign.model.WeChatRegisterParam;
import com.lhiot.healthygood.mapper.user.DoctorCustomerMapper;
import com.lhiot.healthygood.mapper.user.FruitDoctorMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Description:鲜果师微信用户服务类
 *
 * @author yijun
 * @date 2018/07/26
 */
@Service
@Transactional
@Slf4j
public class FruitDoctorUserService {

    private final FruitDoctorMapper fruitDoctorMapper;
    private final DoctorCustomerMapper doctorCustomerMapper;
    private DictionaryClient dictionaryClient;
    private final BaseUserServerFeign baseUserServerFeign;
    private final ApplicationEventPublisher publisher;



    @Autowired
    public FruitDoctorUserService(FruitDoctorMapper fruitDoctorMapper,
                                  DoctorCustomerMapper doctorCustomerMapper, DictionaryClient dictionaryClient,
                                  BaseUserServerFeign baseUserServerFeign,
                                  ApplicationEventPublisher publisher) {
        this.fruitDoctorMapper = fruitDoctorMapper;
        this.doctorCustomerMapper = doctorCustomerMapper;
        this.dictionaryClient = dictionaryClient;
        this.baseUserServerFeign = baseUserServerFeign;
        this.publisher = publisher;
    }

    /**
     * Description:新增鲜果师微信用户
     *
     * @param
     * @return
     * @date 2018/07/26 12:08:13
     */
    public Tips create(WeChatRegisterParam weChatRegisterParam) {
        ResponseEntity responseBaseUser = baseUserServerFeign.registerByWeChat(weChatRegisterParam);
        if (responseBaseUser.getStatusCode().isError()) {
            return Tips.warn((String) responseBaseUser.getBody());
        }
        //绑定鲜果师
        String createdUri = responseBaseUser.getHeaders().getFirst(HttpHeaders.LOCATION);
        int i = createdUri.lastIndexOf("/");
        createdUri = createdUri.substring(i + 1, createdUri.length());
        ResponseEntity<UserDetailResult> baseUser = baseUserServerFeign.findById(Long.valueOf(createdUri));
        DoctorCustomer doctorCustomer = new DoctorCustomer();
        if (baseUser.getStatusCode().is2xxSuccessful()) {
            doctorCustomer.setUserId(baseUser.getBody().getId());
        }
        doctorCustomer.setRemark(baseUser.getBody().getNickname());
        doctorCustomer.setDoctorId(weChatRegisterParam.getDoctorId());
        if (doctorCustomerMapper.create(doctorCustomer) <= 0) {
            return Tips.warn("鲜果师与客户关联失败！");
        }
        return Tips.info("用户创建成功").data((UserDetailResult) baseUser.getBody());
    }


    /**
     * 通过微信返回用户详细信息转换成系统用户
     *
     * @param userStr
     * @return
     */
    public WeChatRegisterParam convert(String userStr) throws IOException {
        ObjectMapper om = new ObjectMapper();
        Map<String, Object> wxUserMap = om.readValue(userStr, Map.class);
        WeChatRegisterParam weChatRegisterParam = new WeChatRegisterParam();
        String nickname = StringUtils.replaceEmoji(wxUserMap.get("nickname").toString(), "");
        weChatRegisterParam.setNickname(nickname);
        String wxSex = String.valueOf(wxUserMap.get("sex"));
        List list = dictionaryClient.dictionary("sex").get().getEntries();
        Optional<Dictionary> optional = dictionaryClient.dictionary("sex");
        if (optional.isPresent()) {
            Dictionary sex = optional.get();
            if (Objects.nonNull(sex)) {
                Dictionary.Entry man = sex.getEntries().stream().filter(entry -> matches(wxSex, entry)).findFirst().orElse(null);
                if (Objects.nonNull(man)) {
                    weChatRegisterParam.setSex(man.getCode());
                }
                Dictionary.Entry woman = sex.getEntries().stream().filter(entry -> matches(wxSex, entry)).findFirst().orElse(null);
                if (Objects.nonNull(woman)) {
                    weChatRegisterParam.setSex(woman.getCode());
                }
            } else {
                weChatRegisterParam.setSex("UNKNOWN");
            }
        }
        /*Dictionary entry = dictionaryClient.dictionary("sex").get();//性别
        if (Objects.isNull(entry)){
            weChatRegisterParam.setSex(entry.getName());
        }*/
        weChatRegisterParam.setAvatar(wxUserMap.get("headimgurl").toString());
        weChatRegisterParam.setAddress(wxUserMap.get("city") + "" + wxUserMap.get("country") + "" + wxUserMap.get("province"));

        String unionid = (String) wxUserMap.get("unionid");
        if (StringUtils.isNotBlank(unionid)) {
            weChatRegisterParam.setUnionId(unionid);
        }
        weChatRegisterParam.setOpenId(wxUserMap.get("openid").toString());
        return weChatRegisterParam;
    }


    private boolean matches(String sex, Dictionary.Entry entry) {
        if (Objects.equals(sex, "0") && Objects.equals(entry.getCode(), "WOMAN")) {
            return true;
        }

        if (Objects.equals(sex, "1") && Objects.equals(entry.getCode(), "MAN")) {
            return true;
        }
        return false;
    }

    /**
     * 注册时绑定手机号码，发送模板消息
     * @param phone 待发送验证码手机号
     */
    public void bandPhoneSendTemplateMessage(String phone) {

        //发送模板消息
        publisher.publishEvent(new SendCaptchaSmsEvent(phone));
    }


}

