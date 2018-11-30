package com.lhiot.healthygood.service.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leon.microx.util.Jackson;
import com.leon.microx.util.StringUtils;
import com.leon.microx.web.result.Tips;
import com.lhiot.dc.dictionary.DictionaryClient;
import com.lhiot.dc.dictionary.module.Dictionary;
import com.lhiot.healthygood.domain.template.TemplateMessageEnum;
import com.lhiot.healthygood.domain.user.*;
import com.lhiot.healthygood.feign.BaseUserServerFeign;
import com.lhiot.healthygood.feign.ThirdpartyServerFeign;
import com.lhiot.healthygood.mapper.user.DoctorUserMapper;
import com.lhiot.healthygood.mapper.user.FruitDoctorMapper;
import com.lhiot.healthygood.type.FruitDoctorOrderExchange;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
* Description:鲜果师微信用户服务类
* @author yijun
* @date 2018/07/26
*/
@Service
@Transactional
public class FruitDoctorUserService {

    private final FruitDoctorMapper fruitDoctorMapper;
    private final DoctorUserMapper doctorUserMapper;
    private  DictionaryClient dictionaryClient;
    private final BaseUserServerFeign baseUserServerFeign;
    private final ThirdpartyServerFeign thirdpartyServerFeign;

    private final RabbitTemplate rabbit;

    @Autowired
    public FruitDoctorUserService(FruitDoctorMapper fruitDoctorMapper,
                                  DictionaryClient dictionaryClient, BaseUserServerFeign baseUserServerFeign, ThirdpartyServerFeign thirdpartyServerFeign,
                                  DoctorUserMapper doctorUserMapper, RabbitTemplate rabbit) {
        this.fruitDoctorMapper = fruitDoctorMapper;
        this.dictionaryClient = dictionaryClient;
        this.baseUserServerFeign = baseUserServerFeign;
        this.thirdpartyServerFeign = thirdpartyServerFeign;
        this.doctorUserMapper = doctorUserMapper;
        this.rabbit = rabbit;
    }

    /** 
    * Description:新增鲜果师微信用户
    *  
    * @param
    * @return
    * @date 2018/07/26 12:08:13
    */  
    public Tips create(WeChatRegisterParam weChatRegisterParam){
        ResponseEntity responseBaseUser=baseUserServerFeign.registerByWeChat(weChatRegisterParam);
        if (responseBaseUser.getStatusCode().isError()){
            return Tips.warn((String) responseBaseUser.getBody());
        }
        //绑定鲜果师
        String createdUri = responseBaseUser.getHeaders().getFirst(HttpHeaders.LOCATION);
        int i = createdUri.lastIndexOf("/");
        createdUri = createdUri.substring(i+1,createdUri.length());
        ResponseEntity<UserDetailResult> baseUser = baseUserServerFeign.findById(Long.valueOf(createdUri));
        DoctorUser doctorUser=new DoctorUser();
        if (baseUser.getStatusCode().is2xxSuccessful()){
            doctorUser.setUserId(baseUser.getBody().getId());
        }
        doctorUser.setRemark(baseUser.getBody().getNickname());
        doctorUser.setDoctorId(weChatRegisterParam.getDoctorId());
        if (doctorUserMapper.create(doctorUser)<=0){
            return Tips.warn("鲜果师与客户关联失败！");
        }
        return Tips.info("用户创建成功").data((UserDetailResult)baseUser.getBody());
    }

   /* public FruitDoctorUser selectByOpenId(String openId){

        UserDetailResult userDetailResult = baseUserServerFeign.findByOpenId(openId).getBody();
        //查询是否为鲜果师成员
        FruitDoctorUser fruitDoctorUser= new FruitDoctorUser();
        fruitDoctorUser.setAvatar(userDetailResult.getAvatar());
        fruitDoctorUser.setBaseuserId(userDetailResult.getBaseUserId());
        fruitDoctorUser.setNickname(userDetailResult.getNickname());
        fruitDoctorUser.setGender(userDetailResult.getSex());
        fruitDoctorUser.setOpenId(userDetailResult.getOpenId());

        if(Objects.nonNull(fruitDoctorUser)){
            FruitDoctor fruitDoctor= fruitDoctorMapper.selectByUserId(fruitDoctorUser.getId());
            //培训通过的鲜果师
            if(Objects.nonNull(fruitDoctor)&&!Objects.equals(fruitDoctor.getDoctorLevel(), DoctorLevel.TRAINING.toString())){
                fruitDoctorUser.setFruitDoctor(true);
            }
            //去基础用户中查询鲜果币信息
            ResponseEntity<BaseUser> baseUserResponseEntity= baseUserServerFeign.findBaseUserById(fruitDoctorUser.getBaseuserId());
            if (Objects.nonNull(baseUserResponseEntity)&&baseUserResponseEntity.getStatusCodeValue()<400){
                fruitDoctorUser.setCurrency(baseUserResponseEntity.getBody().getCurrency());
            }
            //查询用户是否绑定了鲜果师
            Long userId = fruitDoctorUser.getId();
            DoctorUser doctorUser = doctorUserMapper.selectByUserId(userId);
            if(Objects.nonNull(doctorUser)){
                fruitDoctorUser.setDoctorId(doctorUser.getDoctorId());
            }
        }
        return fruitDoctorUser;
    }*/

    /**
     * 通过微信返回用户详细信息转换成系统用户
     * @param userStr
     * @return
     */
    public WeChatRegisterParam convert(String userStr) throws IOException {
        ObjectMapper om = new ObjectMapper();
        Map<String,Object> wxUserMap=om.readValue(userStr, Map.class);
        WeChatRegisterParam weChatRegisterParam=new WeChatRegisterParam();
        String nickname= StringUtils.replaceEmoji(wxUserMap.get("nickname").toString(),"");
        weChatRegisterParam.setNickname(nickname);
        String wxSex=String.valueOf(wxUserMap.get("sex"));
       List list = dictionaryClient.dictionary("sex").get().getEntries();
        Optional<Dictionary> optional = dictionaryClient.dictionary("sex");
        if (optional.isPresent()){
            Dictionary sex = optional.get();
            if (Objects.nonNull(sex)){
                Dictionary.Entry man = sex.getEntries().stream().filter(entry -> matches(wxSex, entry)).findFirst().orElse(null);
                if (Objects.nonNull(man)){
                    weChatRegisterParam.setSex(man.getCode());
                }
                Dictionary.Entry woman = sex.getEntries().stream().filter(entry -> matches(wxSex, entry)).findFirst().orElse(null);
                if (Objects.nonNull(woman)){
                    weChatRegisterParam.setSex(woman.getCode());
                }
            }else {
                weChatRegisterParam.setSex("UNKNOWN");
            }
        }
        /*Dictionary entry = dictionaryClient.dictionary("sex").get();//性别
        if (Objects.isNull(entry)){
            weChatRegisterParam.setSex(entry.getName());
        }*/
        weChatRegisterParam.setAvatar(wxUserMap.get("headimgurl").toString());
        weChatRegisterParam.setAddress(wxUserMap.get("city")+""+wxUserMap.get("country")+""+wxUserMap.get("province"));

        String unionid = (String)wxUserMap.get("unionid");
        if(StringUtils.isNotBlank(unionid)){
            weChatRegisterParam.setUnionId(unionid);
        }
        weChatRegisterParam.setOpenId(wxUserMap.get("openid").toString());
        return weChatRegisterParam;
    }


    private boolean matches(String sex, Dictionary.Entry entry){
        if (Objects.equals(sex,"0") && Objects.equals(entry.getCode(),"WOMAN")){
            return true;
        }

        if (Objects.equals(sex,"1") && Objects.equals(entry.getCode(),"MAN")){
            return true;
        }
        return false;
    }

    /**
     * 绑定手机号码，发送模板消息
     * @param userId
     * @param userName
     * @throws JsonProcessingException
     * @throws AmqpException 
     */
    public void bandPhoneSendTemplateMessage(Long userId,String userName) throws AmqpException, JsonProcessingException {
    	String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    	KeywordValue keywordValue = new KeywordValue();
    	keywordValue.setTemplateType(TemplateMessageEnum.MEMBER_SHIP);
    	keywordValue.setKeyword1Value(userName);
    	keywordValue.setKeyword2Value(currentTime);
    	keywordValue.setSendToDoctor(true);
    	keywordValue.setUserId(userId);
    	
    	//发送模板消息
        rabbit.convertAndSend(FruitDoctorOrderExchange.FRUIT_TEMPLATE_MESSAGE.getExchangeName(),
        		FruitDoctorOrderExchange.FRUIT_TEMPLATE_MESSAGE.getQueueName(), Jackson.json(keywordValue));
    }
}

