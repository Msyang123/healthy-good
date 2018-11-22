package com.lhiot.healthygood.api.user;

import com.leon.microx.web.result.Tips;
import com.lhiot.healthygood.common.PagerResultObject;
import com.lhiot.healthygood.domain.doctor.RegisterApplication;
import com.lhiot.healthygood.domain.doctor.SettlementApplication;
import com.lhiot.healthygood.domain.template.CaptchaTemplate;
import com.lhiot.healthygood.domain.template.FreeSignName;
import com.lhiot.healthygood.domain.user.CaptchaParam;
import com.lhiot.healthygood.domain.user.DoctorUser;
import com.lhiot.healthygood.domain.user.FruitDoctor;
import com.lhiot.healthygood.domain.user.ValidateParam;
import com.lhiot.healthygood.entity.SettlementStatus;
import com.lhiot.healthygood.feign.user.ThirdpartyServerFeign;
import com.lhiot.healthygood.service.doctor.RegisterApplicationService;
import com.lhiot.healthygood.service.doctor.SettlementApplicationService;
import com.lhiot.healthygood.service.user.DoctorUserService;
import com.lhiot.healthygood.service.user.FruitDoctorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Objects;

@Api(description = "鲜果师申请记录接口")
@Slf4j
@RestController
@RequestMapping("/fruit-doctor")
public class FruitDoctorApi {
    private final ThirdpartyServerFeign thirdpartyServerFeign;
    private final RegisterApplicationService registerApplicationService;
    private final SettlementApplicationService settlementApplicationService;
    private final DoctorUserService doctorUserService;
    private final FruitDoctorService fruitDoctorService;
    @Autowired
    public FruitDoctorApi(ThirdpartyServerFeign thirdpartyServerFeign, RegisterApplicationService registerApplicationService, SettlementApplicationService settlementApplicationService, DoctorUserService doctorUserService, FruitDoctorService fruitDoctorService) {
        this.thirdpartyServerFeign = thirdpartyServerFeign;
        this.registerApplicationService = registerApplicationService;
        this.settlementApplicationService = settlementApplicationService;
        this.doctorUserService = doctorUserService;
        this.fruitDoctorService = fruitDoctorService;
    }

    @GetMapping("/sms/captcha")
    @ApiOperation(value = "发送鲜果师申请验证码")
    @ApiImplicitParam(paramType = "query", name = "phone", value = "发送鲜果师申请验证码对应手机号", required = true, dataType = "String")
    public ResponseEntity captcha(@RequestParam String phone) {
        //TODO 需要申请发送短信模板
        CaptchaParam captchaParam=new CaptchaParam();
        captchaParam.setFreeSignName(FreeSignName.FRUIT_DOCTOR);
        captchaParam.setPhoneNumber(phone);
        captchaParam.setApplicationName("和色果膳");
        return thirdpartyServerFeign.captcha(CaptchaTemplate.REGISTER,captchaParam);
    }

    @PostMapping("/qualifications")
    @ApiOperation(value = "添加鲜果师申请记录")
    @ApiImplicitParam(paramType = "body", name = "registerApplication", value = "要添加的鲜果师申请记录", required = true, dataType = "RegisterApplication")
    public ResponseEntity<Object> qualifications(@RequestBody RegisterApplication registerApplication){
        log.debug("添加鲜果师申请记录\t param:{}",registerApplication);
        //到远端验证手机验证码是否正确
        //TODO 需要申请发送短信模板
        ValidateParam smsValidateParam=new ValidateParam();
        smsValidateParam.setVerificationCode(registerApplication.getVerificationCode());
        smsValidateParam.setPhone(registerApplication.getPhone());
        ResponseEntity<String> responseEntity=thirdpartyServerFeign.validate(CaptchaTemplate.REGISTER,smsValidateParam);

        if (responseEntity.getStatusCodeValue() >= 400|| Objects.equals(responseEntity.getBody(),"false")){
            return ResponseEntity.badRequest().body("验证短信失败");
        }
        return ResponseEntity.ok(registerApplicationService.create(registerApplication));
    }


    @PostMapping("/settlement")
    @ApiOperation(value = "申请提现红利")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "id", value = "鲜果师id", required = true, dataType = "Long"),
            @ApiImplicitParam(paramType = "query", name = "amount", value = "申请提现红利", required = true, dataType = "int")
    })
    public ResponseEntity<Tips> create(
            @PathVariable("id") Long id,
            @RequestParam int amount) {
        log.debug("申请提现红利\t param:{}",amount);

        SettlementApplication settlementApplication=new SettlementApplication();

        settlementApplication.setAmount(amount);
        settlementApplication.setDoctorId(id);
        settlementApplication.setCreateTime(new Date());
        settlementApplication.setSettlementStatus(SettlementStatus.UNSETTLED.toString());
        int result=settlementApplicationService.create(settlementApplication);
        Tips tips = new Tips();
        if(result>0) {
            return ResponseEntity.ok(Tips.info("申请成功！"));
        }
        return ResponseEntity.badRequest().body(Tips.warn("申请失败！"));
    }

    @PostMapping("/relation")
    @ApiOperation(value = "添加鲜果师客户 关注鲜果师(绑定)")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "doctorId", value = "鲜果师id", required = true, dataType = "Long"),
            @ApiImplicitParam(paramType = "body", name = "doctorUser", value = "要添加的鲜果师客户", required = true, dataType = "DoctorUser")
    })
    public ResponseEntity<Tips> bindingDoctor(@PathVariable Long doctorId, @RequestBody DoctorUser doctorUser) {
        log.debug("添加鲜果师客户\t param:{}-{}",doctorId,doctorUser);
        doctorUser.setDoctorId(doctorId);
        doctorUserService.create(doctorUser);
        return ResponseEntity.ok(Tips.info("绑定成功！"));
    }

    @PutMapping("/remark")
    @ApiOperation(value = "鲜果师修改用户备注")
    @ApiImplicitParam(paramType = "body", name = "doctorUser", value = "鲜果师修改用户备注实体信息", required = true, dataType = "DoctorUser")
    public ResponseEntity<Tips> updateRemarkName(@RequestBody DoctorUser doctorUser) {
        log.debug("鲜果师修改用户备注\t param:{}",doctorUser);
        int result=doctorUserService.updateRemarkName(doctorUser);
        if(result>0) {
            return ResponseEntity.ok(Tips.info("更新成功"));
        }else {
            return ResponseEntity.ok(Tips.info("更新失败"));
        }
    }


    @GetMapping("/subordinate")
    @ApiOperation(value = "团队列表")
    public ResponseEntity<PagerResultObject<FruitDoctor>> team(FruitDoctor fruitDoctor){
        log.debug("查询鲜果师成员分页列表\t param:{}",fruitDoctor);
        return ResponseEntity.ok(fruitDoctorService.team(fruitDoctor));
    }

    @GetMapping("/page")
    @ApiOperation(value = "查询鲜果师成员分页列表")
    public ResponseEntity<PagerResultObject<FruitDoctor>> pageQuery(FruitDoctor fruitDoctor){
        log.debug("查询鲜果师成员分页列表\t param:{}",fruitDoctor);
        return ResponseEntity.ok(fruitDoctorService.pageList(fruitDoctor));
    }
}
