package com.lhiot.healthygood.event;

import com.lhiot.healthygood.feign.ThirdpartyServerFeign;
import com.lhiot.healthygood.feign.model.AccountAuditParam;
import com.lhiot.healthygood.feign.model.CaptchaParam;
import com.lhiot.healthygood.service.user.FruitDoctorService;
import com.lhiot.healthygood.type.ApplicationType;
import com.lhiot.healthygood.type.CaptchaTemplate;
import com.lhiot.healthygood.type.FreeSignName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class SendSmsListener {

    private final FruitDoctorService fruitDoctorService;
    private final ThirdpartyServerFeign thirdpartyServerFeign;

    @Autowired
    public SendSmsListener(FruitDoctorService fruitDoctorService, ThirdpartyServerFeign thirdpartyServerFeign) {
        this.fruitDoctorService = fruitDoctorService;
        this.thirdpartyServerFeign = thirdpartyServerFeign;
    }



    @Async
    @EventListener
    public void onNotify(SendCaptchaSmsEvent event){
        //调用发送注册鲜果师短信验证码
        CaptchaParam captchaParam=new CaptchaParam();
        captchaParam.setApplicationName(ApplicationType.FRUIT_DOCTOR.getDescription());
        captchaParam.setFreeSignName(FreeSignName.FRUIT_DOCTOR);//TODO 基础服务没有加
        captchaParam.setPhoneNumber(event.getPhone());
        thirdpartyServerFeign.captcha(CaptchaTemplate.REGISTER,captchaParam);
    }

    @Async
    @EventListener
    public void onNotify(SendAccountAuditSmsEvent event){
        //调用发送审核账户结果短信通知
        AccountAuditParam parameters=new AccountAuditParam();
        parameters.setAccount(event.getAccount());
        parameters.setApplicationName(ApplicationType.FRUIT_DOCTOR.getDescription());
        parameters.setFreeSignName(FreeSignName.FRUIT_DOCTOR);//TODO 基础服务没有加
        parameters.setPhoneNumber(event.getPhone());
        parameters.setStatus(event.getAccountAuditStatus());
        thirdpartyServerFeign.accountAudit(parameters);
    }
}
