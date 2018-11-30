package com.lhiot.healthygood.api.user;

import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.leon.microx.web.session.Sessions;
import com.lhiot.healthygood.domain.doctor.*;
import com.lhiot.healthygood.domain.template.CaptchaTemplate;
import com.lhiot.healthygood.domain.template.FreeSignName;
import com.lhiot.healthygood.domain.user.CaptchaParam;
import com.lhiot.healthygood.domain.user.DoctorUser;
import com.lhiot.healthygood.domain.user.FruitDoctor;
import com.lhiot.healthygood.domain.user.ValidateParam;
import com.lhiot.healthygood.feign.ThirdpartyServerFeign;
import com.lhiot.healthygood.service.doctor.CardUpdateLogService;
import com.lhiot.healthygood.service.doctor.DoctorAchievementLogService;
import com.lhiot.healthygood.service.doctor.RegisterApplicationService;
import com.lhiot.healthygood.service.doctor.SettlementApplicationService;
import com.lhiot.healthygood.service.user.DoctorUserService;
import com.lhiot.healthygood.service.user.FruitDoctorService;
import com.lhiot.healthygood.type.DateTypeEnum;
import com.lhiot.healthygood.type.PeriodType;
import com.lhiot.healthygood.type.SettlementStatus;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
    private final DoctorAchievementLogService doctorAchievementLogService;
    private final CardUpdateLogService cardUpdateLogService;
    private Sessions session;
    @Autowired
    public FruitDoctorApi(ThirdpartyServerFeign thirdpartyServerFeign, RegisterApplicationService registerApplicationService, SettlementApplicationService settlementApplicationService, DoctorUserService doctorUserService, FruitDoctorService fruitDoctorService, DoctorAchievementLogService doctorAchievementLogService, CardUpdateLogService cardUpdateLogService, Sessions session) {
        this.thirdpartyServerFeign = thirdpartyServerFeign;
        this.registerApplicationService = registerApplicationService;
        this.settlementApplicationService = settlementApplicationService;
        this.doctorUserService = doctorUserService;
        this.fruitDoctorService = fruitDoctorService;
        this.doctorAchievementLogService = doctorAchievementLogService;
        this.cardUpdateLogService = cardUpdateLogService;
        this.session = session;
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
    public ResponseEntity<Pages<FruitDoctor>> team(FruitDoctor fruitDoctor){
        log.debug("查询鲜果师成员分页列表\t param:{}",fruitDoctor);
        return ResponseEntity.ok(fruitDoctorService.subordinate(fruitDoctor));
    }

    @GetMapping("/page")
    @ApiOperation(value = "查询鲜果师成员分页列表")
    public ResponseEntity<Pages<FruitDoctor>> pageQuery(FruitDoctor fruitDoctor){
        log.debug("查询鲜果师成员分页列表\t param:{}",fruitDoctor);
        return ResponseEntity.ok(fruitDoctorService.pageList(fruitDoctor));
    }

    @GetMapping("/incomes/detail")
    @ApiOperation(value = "收支明细")
    public ResponseEntity<Pages<DoctorAchievementLog>> pageQuery(DoctorAchievementLog doctorAchievementLog){
        log.debug("收支明细\t param:{}", doctorAchievementLog);

        return ResponseEntity.ok(doctorAchievementLogService.pageList(doctorAchievementLog));
    }

    @GetMapping("/incomes")
    @ApiImplicitParam(paramType = "path", name = "id", value = "鲜果师id", required = true, dataType = "Long")
    @ApiOperation(value = "我的收入")
    public ResponseEntity<IncomeStat> myIncome(HttpServletRequest request){
        String sessionId = session.id(request);
        Long doctorId = (Long) session.user(sessionId).getUser().get("doctorId");
        return ResponseEntity.ok(doctorAchievementLogService.myIncome(doctorId));
    }

    @GetMapping("/member-achievements/{doctorId}")
    @ApiImplicitParam(paramType = "path", name = "doctorId", value = "下级鲜果师id", required = true, dataType = "Long")
    @ApiOperation(value = "我的团队的个人业绩")
    public ResponseEntity<TeamAchievement> teamAchievement(@PathVariable("id") Long id){
        log.debug("我的团队的个人业绩\t param:{}",id);

        return ResponseEntity.ok(doctorAchievementLogService.teamAchievement(id));
    }

    @ApiOperation("统计本期和上期的日周月季度的业绩")
    @ApiImplicitParam(paramType = "query", name = "dateType", dataType = "DateTypeEnum", required = true, value = "统计的类型")
    @GetMapping("/achievement/period")
    public ResponseEntity<?> findAchievement(HttpServletRequest request,@RequestParam DateTypeEnum dateType){
        String sessionId = session.id(request);
        Long doctorId = (Long) session.user(sessionId).getUser().get("doctorId");
        //统计本期
        Achievement current =
                doctorAchievementLogService.achievement(dateType, PeriodType.current, doctorId, true, false,null);
        //统计上期
        Achievement last =
                doctorAchievementLogService.achievement(dateType, PeriodType.last, doctorId, true, false,null);
        //构建返回值
        Map<String,Object> achievementMap = new HashMap<>();
        achievementMap.put("current", current);
        achievementMap.put("last", last);
        return ResponseEntity.ok(achievementMap);
    }

    @ApiOperation("统计今日业绩订单数及总的业绩")
    @GetMapping("/achievement")
    public ResponseEntity<?> findTodayAchievement(HttpServletRequest request){
        String sessionId = session.id(request);
        Long doctorId = (Long) session.user(sessionId).getUser().get("doctorId");
        //统计今天订单数和业绩
        Achievement current =
                doctorAchievementLogService.achievement(DateTypeEnum.DAY, PeriodType.current, doctorId, true, false,null);
        //统计总的订单数
        Achievement total =
                doctorAchievementLogService.achievement(DateTypeEnum.DAY, PeriodType.current, doctorId, true, true,null);
        //构建返回值
        current.setSummaryAmount(total.getSalesAmount());
        return ResponseEntity.ok(current);
    }

    @PostMapping("/bank-card")
    @ApiOperation(value = "银行卡添加")
    @ApiImplicitParam(paramType = "body", name = "cardUpdateLog", value = "要添加的", required = true, dataType = "CardUpdateLog")
    public ResponseEntity<Integer> create(@RequestBody CardUpdateLog cardUpdateLog) {
        log.debug("添加\t param:{}",cardUpdateLog);

        return ResponseEntity.ok(cardUpdateLogService.create(cardUpdateLog));
    }

    @ApiOperation(value = "查询鲜果师银行卡信息", notes = "根据session里的doctorId查询")
    @GetMapping("/bank-card")
    public ResponseEntity<CardUpdateLog> findCardUpdateLog(HttpServletRequest request) {
        String sessionId = session.id(request);
        Long doctorId = (Long) session.user(sessionId).getUser().get("doctorId");
        CardUpdateLog cardUpdateLog = new CardUpdateLog();
        cardUpdateLog.setDoctorId(doctorId);
        return ResponseEntity.ok(cardUpdateLogService.selectByCard(cardUpdateLog));
    }


}
