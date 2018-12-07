package com.lhiot.healthygood.api.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.leon.microx.web.session.Sessions;
import com.leon.microx.web.swagger.ApiParamType;
import com.lhiot.healthygood.domain.doctor.*;
import com.lhiot.healthygood.domain.user.DoctorUser;
import com.lhiot.healthygood.domain.user.FruitDoctor;
import com.lhiot.healthygood.domain.user.KeywordValue;
import com.lhiot.healthygood.domain.user.ValidateParam;
import com.lhiot.healthygood.feign.BaseUserServerFeign;
import com.lhiot.healthygood.feign.ThirdpartyServerFeign;
import com.lhiot.healthygood.feign.model.UserDetailResult;
import com.lhiot.healthygood.service.doctor.CardUpdateLogService;
import com.lhiot.healthygood.service.doctor.DoctorAchievementLogService;
import com.lhiot.healthygood.service.doctor.RegisterApplicationService;
import com.lhiot.healthygood.service.doctor.SettlementApplicationService;
import com.lhiot.healthygood.service.user.DoctorUserService;
import com.lhiot.healthygood.service.user.FruitDoctorService;
import com.lhiot.healthygood.type.*;
import com.lhiot.healthygood.util.PinyinTool;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.amqp.AmqpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Api(description = "鲜果师申请记录接口")
@Slf4j
@RestController
@RequestMapping("/fruit-doctors")
public class FruitDoctorApi {
    private final ThirdpartyServerFeign thirdpartyServerFeign;
    private final RegisterApplicationService registerApplicationService;
    private final SettlementApplicationService settlementApplicationService;
    private final DoctorUserService doctorUserService;
    private final FruitDoctorService fruitDoctorService;
    private final DoctorAchievementLogService doctorAchievementLogService;
    private final CardUpdateLogService cardUpdateLogService;
    private final BaseUserServerFeign baseUserServerFeign;
    private Sessions session;

    @Autowired
    public FruitDoctorApi(ThirdpartyServerFeign thirdpartyServerFeign, RegisterApplicationService registerApplicationService, SettlementApplicationService settlementApplicationService, DoctorUserService doctorUserService, FruitDoctorService fruitDoctorService, DoctorAchievementLogService doctorAchievementLogService, CardUpdateLogService cardUpdateLogService, BaseUserServerFeign baseUserServerFeign, Sessions session) {
        this.thirdpartyServerFeign = thirdpartyServerFeign;
        this.registerApplicationService = registerApplicationService;
        this.settlementApplicationService = settlementApplicationService;
        this.doctorUserService = doctorUserService;
        this.fruitDoctorService = fruitDoctorService;
        this.doctorAchievementLogService = doctorAchievementLogService;
        this.cardUpdateLogService = cardUpdateLogService;
        this.baseUserServerFeign = baseUserServerFeign;
        this.session = session;
    }

    @Sessions.Uncheck
    @GetMapping("/sms/captcha")
    @ApiOperation(value = "发送鲜果师申请验证码")
    @ApiImplicitParam(paramType = "query", name = "phone", value = "发送鲜果师申请验证码对应手机号", required = true, dataType = "String")
    public ResponseEntity captcha(@RequestParam String phone) {
        //TODO 需要申请发送短信模板
        fruitDoctorService.bandPhoneSendTemplateMessage(phone);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/qualifications")
    @ApiOperation(value = "添加鲜果师申请记录")
    @ApiImplicitParam(paramType = "body", name = "registerApplication", value = "要添加的鲜果师申请记录", required = true, dataType = "RegisterApplication")
    public ResponseEntity qualifications(Sessions.User user, @RequestBody RegisterApplication registerApplication) {
        log.debug("添加鲜果师申请记录\t param:{}", registerApplication);
        String userId = user.getUser().get("userId").toString();
        registerApplication.setUserId(Long.valueOf(userId));
        //TODO 需要申请发送短信模板
        ValidateParam smsValidateParam = new ValidateParam();
        smsValidateParam.setCode(registerApplication.getVerificationCode());
        smsValidateParam.setPhoneNumber(registerApplication.getPhone());
        ResponseEntity responseEntity = thirdpartyServerFeign.validate(CaptchaTemplate.REGISTER, smsValidateParam);

        if (responseEntity.getStatusCode().isError()) {
            return ResponseEntity.badRequest().body(responseEntity.getBody());
        }
        return ResponseEntity.ok(registerApplicationService.create(registerApplication));
    }

    @Sessions.Uncheck
    @ApiOperation(value = "根据id更新鲜果师申请记录(后台)")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = ApiParamType.PATH, name = "id", value = "主键id", dataType = "Long", required = true),
            @ApiImplicitParam(paramType = ApiParamType.BODY, name = "registerApplication", value = "要更新的鲜果师申请记录", required = true, dataType = "RegisterApplication")
    })
    @PutMapping("/qualifications/{id}")
    public ResponseEntity update(@PathVariable("id") Long id, @RequestBody RegisterApplication registerApplication) throws AmqpException, JsonProcessingException {
        log.debug("根据id更新鲜果师申请记录\t id:{} param:{}", id, registerApplication);

        RegisterApplication findLastApplication = registerApplicationService.findLastApplicationById(registerApplication.getUserId());
        if (Objects.isNull(findLastApplication)) {
            return ResponseEntity.badRequest().body("未找到该用户审核记录");
        }

        registerApplication.setId(id);
        boolean updated = registerApplicationService.updateById(registerApplication) > 0;
        if (!updated) {
            ResponseEntity.badRequest().body("修改鲜果师申请记录失败");
        }
        // 发送模板消息
        registerApplicationService.doctorApplicationSendToQueue(registerApplication.getAuditStatus(), registerApplication.getUserId());
        // 审核通过 新增鲜果师成员记录
        if (Objects.equals(AuditStatus.AGREE, registerApplication.getAuditStatus())) {
            Tips tips = fruitDoctorService.create(registerApplication);
            if (tips.err()) {
                return ResponseEntity.badRequest().body(Tips.warn(tips.getMessage()));
            }
        }
        if (Objects.equals(registerApplication.getAuditStatus(), AuditStatus.AGREE.toString())) {
            return ResponseEntity.ok("审核通过");
        }
        if (Objects.equals(registerApplication.getAuditStatus(), AuditStatus.REJECT.toString())) {
            return ResponseEntity.ok("审核不通过");
        }
        return ResponseEntity.ok("审核不通过");
    }

    @Sessions.Uncheck
    @ApiOperation(value = "根据条件分页查询鲜果师申请记录列表(后台)", response = RegisterApplication.class, responseContainer = "Set")
    @ApiImplicitParam(paramType = ApiParamType.BODY, name = "registerApplication", value = "鲜果师申请信息", dataType = "RegisterApplication", required = true)
    @PostMapping("/qualifications/pages")
    public ResponseEntity search(@RequestBody RegisterApplication registerApplication) {
        log.debug("根据条件分页查询鲜果师申请记录列表\t param:{}", registerApplication);

        Pages<RegisterApplication> pages = registerApplicationService.pageList(registerApplication);
        return ResponseEntity.ok(pages);
    }

    @PostMapping("/settlement")
    @ApiOperation(value = "申请提现红利")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "amount", value = "申请提现红利", required = true, dataType = "int")
    })
    public ResponseEntity create(Sessions.User user,
                                 @RequestParam int amount) {
        log.debug("申请提现红利\t param:{}", amount);
        String userId = user.getUser().get("userId").toString();
        FruitDoctor fruitDoctor = fruitDoctorService.selectByUserId(Long.valueOf(userId));
        if (Objects.isNull(fruitDoctor)) {
            return ResponseEntity.badRequest().body(Tips.warn("鲜果师不存在"));
        }
        SettlementApplication settlementApplication = new SettlementApplication();

        settlementApplication.setAmount(amount);
        settlementApplication.setDoctorId(fruitDoctor.getId());
        settlementApplication.setCreateAt(Date.from(Instant.now()));
        settlementApplication.setSettlementStatus(SettlementStatus.UNSETTLED);
        int result = settlementApplicationService.create(settlementApplication);
        Tips tips = new Tips();
        if (result > 0) {
            return ResponseEntity.ok(Tips.info("申请成功！"));
        }
        return ResponseEntity.badRequest().body(Tips.warn("申请失败！"));
    }

    @Sessions.Uncheck
    @ApiOperation(value = "结算申请修改-已结算(后台)")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = ApiParamType.PATH, name = "id", value = "结算申请id", required = true, dataType = "Long"),
            @ApiImplicitParam(paramType = ApiParamType.BODY, name = "settlementApplication", value = "要修改的结算申请信息",dataType = "SettlementApplication", required = true)
    })
    @PutMapping("/settlement/{id}")
    public ResponseEntity updateSettlement(@PathVariable("id") Long id, @RequestBody SettlementApplication settlementApplication) {
        log.debug("结算申请修改-已结算\t id:{},param:{}", id, settlementApplication);

        // 已结算才进行修改，且只能结算一次，成功后不可修改
        if (Objects.equals(SettlementStatus.SUCCESS,settlementApplication.getSettlementStatus())){
            SettlementApplication findSettlementApplication = settlementApplicationService.selectById(id);
            // 结算用户是否一致
            if (!Objects.equals(findSettlementApplication.getDoctorId(),settlementApplication.getDoctorId())) {
                return ResponseEntity.badRequest().body(Tips.warn("要结算的用户不一致，结算失败"));
            }
            // 该条结算记录是否已结算过
            if (Objects.equals(SettlementStatus.SUCCESS,findSettlementApplication.getSettlementStatus())){
                return ResponseEntity.badRequest().body(Tips.warn("请勿重复结算"));
            }
            FruitDoctor findFruitDoctor = fruitDoctorService.selectById(settlementApplication.getDoctorId());
            if (Objects.isNull(findFruitDoctor)) {
                return ResponseEntity.badRequest().body(Tips.warn("鲜果师不存在！"));
            }
            // 该鲜果师的可结算金额是否大于申请结算金额
            if (settlementApplication.getAmount() > findFruitDoctor.getBalance()){
                return ResponseEntity.badRequest().body(Tips.warn("申请结算金额大于用户可结算金额，结算失败"));
            }

            // 进行结算和鲜果师可结算金额扣减
            settlementApplication.setId(id);
            settlementApplication.setDealAt(Date.from(Instant.now()));
            Tips tips = settlementApplicationService.updateById(settlementApplication, findFruitDoctor);
            return tips.err() ? ResponseEntity.badRequest().body(Tips.warn(tips.getMessage())) : ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body(Tips.warn("结算状态不正确！"));
    }

    @Sessions.Uncheck
    @ApiOperation(value = "结算申请分页查询(后台)")
    @ApiImplicitParam(paramType = ApiParamType.BODY, name = "settlementApplication", value = "结算申请分页查询条件", dataType = "SettlementApplication", required = true)
    @PostMapping("/settlement/pages")
    public ResponseEntity search(@RequestBody SettlementApplication settlementApplication){
        log.debug("结算申请分页查询\t param:{}", settlementApplication);

        Pages<SettlementApplication> settlementApplicationPages = settlementApplicationService.pageList(settlementApplication);
        return ResponseEntity.ok(settlementApplicationPages);
    }

    @PostMapping("/relation")
    @ApiOperation(value = "添加鲜果师客户 关注鲜果师(绑定)")
    @ApiImplicitParam(paramType = "body", name = "doctorUser", value = "要添加的鲜果师客户", required = true, dataType = "DoctorUser")
    public ResponseEntity bindingDoctor(Sessions.User user, @RequestBody DoctorUser doctorUser) {
        String userId = user.getUser().get("userId").toString();
        FruitDoctor fruitDoctor = fruitDoctorService.selectByUserId(Long.valueOf(userId));
        if (Objects.isNull(fruitDoctor)) {
            return ResponseEntity.badRequest().body(Tips.warn("鲜果师不存在"));
        }
        DoctorUser dUser = doctorUserService.selectByDoctorId(fruitDoctor.getId());
        if (Objects.nonNull(dUser)) {
            return ResponseEntity.badRequest().body(Tips.warn("您已经绑定该鲜果师了"));
        }
        doctorUser.setDoctorId(fruitDoctor.getId());
        doctorUserService.create(doctorUser);
        return ResponseEntity.ok(Tips.info("绑定成功！"));
    }

    @PutMapping("/remark")
    @ApiOperation(value = "鲜果师修改用户备注")
    @ApiImplicitParam(paramType = "body", name = "doctorUser", value = "鲜果师修改用户备注实体信息", required = true, dataType = "DoctorUser")
    public ResponseEntity updateRemarkName(Sessions.User user, @RequestBody DoctorUser doctorUser) {
        String userId = user.getUser().get("userId").toString();
        FruitDoctor fruitDoctor = fruitDoctorService.selectByUserId(Long.valueOf(userId));
        if (Objects.isNull(fruitDoctor)) {
            return ResponseEntity.badRequest().body(Tips.warn("鲜果师不存在"));
        }
        log.debug("鲜果师修改用户备注\t param:{}", doctorUser);
        doctorUser.setDoctorId(fruitDoctor.getId());
        int result = doctorUserService.updateRemarkName(doctorUser);
        if (result > 0) {
            return ResponseEntity.ok(Tips.info("更新成功"));
        } else {
            return ResponseEntity.badRequest().body(Tips.warn("更新失败"));
        }
    }


    @GetMapping("/subordinate")
    @ApiOperation(value = "团队列表", response = FruitDoctor.class, responseContainer = "Set")
    public ResponseEntity team(Sessions.User user) {
        String userId = user.getUser().get("userId").toString();
        FruitDoctor fruitDoctor = fruitDoctorService.selectByUserId(Long.valueOf(userId));
        if (Objects.isNull(fruitDoctor)) {
            return ResponseEntity.badRequest().body(Tips.warn("鲜果师不存在"));
        }
        FruitDoctor fruitDoctor1 = new FruitDoctor();
        fruitDoctor1.setId(fruitDoctor.getId());
        return ResponseEntity.ok(fruitDoctorService.subordinate(fruitDoctor));
    }

    @GetMapping("/page")
    @ApiOperation(value = "查询鲜果师成员分页列表", response = FruitDoctor.class, responseContainer = "Set")
    public ResponseEntity pageQuery(Sessions.User user, FruitDoctor fruitDoctor1) {
        String userId = user.getUser().get("userId").toString();
        FruitDoctor fruitDoctor = fruitDoctorService.selectByUserId(Long.valueOf(userId));
        if (Objects.isNull(fruitDoctor)) {
            return ResponseEntity.badRequest().body(Tips.warn("鲜果师不存在"));
        }
        log.debug("查询鲜果师成员分页列表\t param:{}", fruitDoctor);
        return ResponseEntity.ok(fruitDoctorService.pageList(fruitDoctor));
    }

    @Sessions.Uncheck
    @PostMapping("/pages")
    @ApiOperation(value = "查询鲜果师成员分页列表（后台）", response = FruitDoctor.class, responseContainer = "Set")
    @ApiImplicitParam(paramType = ApiParamType.BODY, name = "fruitDoctor", value = "要查询的鲜果师信息", dataType = "FruitDoctor", required = true)
    public ResponseEntity search(@RequestBody FruitDoctor fruitDoctor) {
        log.debug("查询鲜果师成员分页列表\t param:{}", fruitDoctor);

        return ResponseEntity.ok(fruitDoctorService.pageList(fruitDoctor));
    }

    @Sessions.Uncheck
    @ApiOperation(value = "查询鲜果师成员详情（后台）", response = FruitDoctor.class)
    @ApiImplicitParam(paramType = ApiParamType.PATH, name = "id", value = "鲜果师id", dataType = "Long", required = true)
    @GetMapping("/{id}")
    public ResponseEntity findById(@PathVariable("id") Long id) {
        log.debug("查询鲜果师成员详情\t id:{}", id);

        FruitDoctor fruitDoctor = fruitDoctorService.selectById(id);
        return ResponseEntity.ok().body(fruitDoctor);
    }

    @Sessions.Uncheck
    @ApiOperation(value = "修改鲜果师成员信息（后台）")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = ApiParamType.PATH, name = "id", value = "鲜果师id", dataType = "Long", required = true),
            @ApiImplicitParam(paramType = ApiParamType.BODY, name = "fruitDoctor", value = "要修改鲜果师成员信息", dataType = "FruitDoctor", required = true)
    })
    @PutMapping("/{id}")
    public ResponseEntity updateById(@PathVariable("id") Long id, @RequestBody FruitDoctor fruitDoctor) {
        log.debug("修改鲜果师成员信息\t id:{},param:{}", id, fruitDoctor);

        fruitDoctor.setId(id);
        return fruitDoctorService.updateById(fruitDoctor) > 0 ? ResponseEntity.ok().build() : ResponseEntity.badRequest().body("修改鲜果师成员信息失败");
    }

    @GetMapping("/incomes/detail")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "page", dataType = "Integer", required = true, value = "多少页"),
            @ApiImplicitParam(paramType = "query", name = "rows", dataType = "Integer", required = true, value = "数据多少条"),
            @ApiImplicitParam(paramType = "query", name = "incomeType", dataTypeClass = IncomeType.class, required = true, value = "收入支出类型")
    })
    @ApiOperation(value = "收支明细", response = DoctorAchievementLog.class, responseContainer = "Set")
    public ResponseEntity pageQuery(Sessions.User user, @RequestParam IncomeType incomeType, @RequestParam Integer page, @RequestParam Integer rows) {
        String userId = user.getUser().get("userId").toString();
        FruitDoctor fruitDoctor = fruitDoctorService.selectByUserId(Long.valueOf(userId));
        if (Objects.isNull(fruitDoctor)) {
            return ResponseEntity.badRequest().body(Tips.warn("鲜果师不存在"));
        }
        DoctorAchievementLog doctorAchievementLog = new DoctorAchievementLog();
        doctorAchievementLog.setIncomeType(incomeType);
        doctorAchievementLog.setDoctorId(fruitDoctor.getId());
        doctorAchievementLog.setRows(rows);
        doctorAchievementLog.setPage(page);
        return ResponseEntity.ok(doctorAchievementLogService.pageList(doctorAchievementLog));
    }

    @GetMapping("/incomes")
    @ApiOperation(value = "我的收入", response = IncomeStat.class, responseContainer = "Set")
    public ResponseEntity myIncome(Sessions.User user) {
        String userId = user.getUser().get("userId").toString();
        FruitDoctor fruitDoctor = fruitDoctorService.selectByUserId(Long.valueOf(userId));
        if (Objects.isNull(fruitDoctor)) {
            return ResponseEntity.badRequest().body(Tips.warn("鲜果师不存在"));
        }
        return ResponseEntity.ok(doctorAchievementLogService.myIncome(fruitDoctor.getId()));
    }

    @Sessions.Uncheck
    @GetMapping("/member-achievements/{doctorId}")
    @ApiOperation(value = "我的团队的个人业绩", response = TeamAchievement.class, responseContainer = "Set")
    @ApiImplicitParam(paramType = "path", name = "doctorId", value = "鲜果师id", required = true, dataType = "Long")
    public ResponseEntity teamAchievement(@PathVariable Long doctorId) {

        return ResponseEntity.ok(doctorAchievementLogService.teamAchievement(doctorId));
    }

    @ApiOperation(value = "统计本期和上期的日周月季度的业绩", response = Achievement.class, responseContainer = "Set")
    @ApiImplicitParam(paramType = "query", name = "dateType", dataTypeClass = DateTypeEnum.class, required = true, value = "统计的类型")
    @GetMapping("/achievement/period")
    public ResponseEntity findAchievement(Sessions.User user, @RequestParam DateTypeEnum dateType) {
        String userId = user.getUser().get("userId").toString();
        FruitDoctor fruitDoctor = fruitDoctorService.selectByUserId(Long.valueOf(userId));
        if (Objects.isNull(fruitDoctor)) {
            return ResponseEntity.badRequest().body(Tips.warn("鲜果师不存在"));
        }
        //统计本期
        Achievement current =
                doctorAchievementLogService.achievement(dateType, PeriodType.current, fruitDoctor.getId(), true, false, null);
        //统计上期
        Achievement last =
                doctorAchievementLogService.achievement(dateType, PeriodType.last, fruitDoctor.getId(), true, false, null);
        //构建返回值
        Map<String, Object> achievementMap = new HashMap<>();
        achievementMap.put("current", current);
        achievementMap.put("last", last);
        return ResponseEntity.ok(achievementMap);
    }

    @ApiOperation(value = "统计今日业绩订单数及总的业绩", response = Achievement.class, responseContainer = "Set")
    @GetMapping("/achievement")
    public ResponseEntity findTodayAchievement(Sessions.User user) {
        String userId = user.getUser().get("userId").toString();
        FruitDoctor fruitDoctor = fruitDoctorService.selectByUserId(Long.valueOf(userId));
        if (Objects.isNull(fruitDoctor)) {
            return ResponseEntity.badRequest().body(Tips.warn("鲜果师不存在"));
        }
        //统计今天订单数和业绩
        Achievement current =
                doctorAchievementLogService.achievement(DateTypeEnum.DAY, PeriodType.current, fruitDoctor.getId(), true, false, null);
        //统计总的订单数
        Achievement total =
                doctorAchievementLogService.achievement(DateTypeEnum.DAY, PeriodType.current, fruitDoctor.getId(), true, true, null);
        //构建返回值
        current.setSummaryAmount(total.getSalesAmount());
        current.setAvatar(fruitDoctor.getAvatar());
        current.setDescription(fruitDoctor.getProfile());
        return ResponseEntity.ok(current);
    }

    @PostMapping("/bank-card")
    @ApiOperation(value = "银行卡添加")
    @ApiImplicitParam(paramType = "body", name = "cardUpdateLog", value = "要添加的", required = true, dataType = "CardUpdateLog")
    public ResponseEntity create(Sessions.User user, @RequestBody CardUpdateLog cardUpdateLog) {
        log.debug("添加\t param:{}", cardUpdateLog);
        String userId = user.getUser().get("userId").toString();
        FruitDoctor fruitDoctor = fruitDoctorService.selectByUserId(Long.valueOf(userId));
        if (Objects.isNull(fruitDoctor)) {
            return ResponseEntity.badRequest().body(Tips.warn("鲜果师不存在"));
        }
        CardUpdateLog cardParam = new CardUpdateLog();
        cardParam.setDoctorId(fruitDoctor.getId());
        CardUpdateLog card = cardUpdateLogService.selectByCard(cardParam);
        if (Objects.nonNull(card)) {
            return ResponseEntity.badRequest().body(Tips.warn("只能添加一张卡"));
        }
        cardUpdateLog.setDoctorId(fruitDoctor.getId());
        cardUpdateLog.setUpdateAt(Date.from(Instant.now()));
        return ResponseEntity.ok(cardUpdateLogService.create(cardUpdateLog));
    }

    @ApiOperation(value = "查询鲜果师银行卡信息", notes = "根据session里的doctorId查询", response = CardUpdateLog.class, responseContainer = "Set")
    @GetMapping("/bank-card")
    public ResponseEntity findCardUpdateLog(Sessions.User user) {
        String userId = user.getUser().get("userId").toString();
        FruitDoctor fruitDoctor = fruitDoctorService.selectByUserId(Long.valueOf(userId));
        if (Objects.isNull(fruitDoctor)) {
            return ResponseEntity.badRequest().body(Tips.warn("鲜果师不存在"));
        }
        CardUpdateLog cardUpdateLog = new CardUpdateLog();
        cardUpdateLog.setDoctorId(fruitDoctor.getId());
        return ResponseEntity.ok(cardUpdateLogService.selectByCard(cardUpdateLog));
    }

    @ApiOperation(value = "查询鲜果师最新申请记录", notes = "查询鲜果师最新申请记录")
    @GetMapping("/new-application")
    public ResponseEntity<RegisterApplication> findLastApplicationById(Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        return ResponseEntity.ok(registerApplicationService.findLastApplicationById(userId));
    }

    @GetMapping("/customers")
    @ApiOperation(value = "查询鲜果师客户列表")
    public ResponseEntity doctorCustomers(Sessions.User user) throws BadHanyuPinyinOutputFormatCombination {
        String userId = user.getUser().get("userId").toString();
        FruitDoctor fruitDoctor = fruitDoctorService.selectByUserId(Long.valueOf(userId));
        if (Objects.isNull(fruitDoctor)) {
            return ResponseEntity.badRequest().body(Tips.warn("鲜果师不存在"));
        }
        List<DoctorUser> doctorUserList = doctorUserService.doctorCustomers(fruitDoctor.getId());
        doctorUserList.stream().forEach(doctorUser -> {
            ResponseEntity userInfoEntity = baseUserServerFeign.findById(Long.valueOf(doctorUser.getUserId()));
            if (userInfoEntity.getStatusCode().isError()) {
                return;
            }
            UserDetailResult userDetailResult = (UserDetailResult) userInfoEntity.getBody();
            doctorUser.setNickname(userDetailResult.getNickname());
            doctorUser.setAvatar(userDetailResult.getAvatar());
            doctorUser.setPhone(userDetailResult.getPhone());
        });
        PinyinTool tool = new PinyinTool();
        Pattern pattern = Pattern.compile("^[a-zA-Z]");

        JSONArray customers = new JSONArray();
        for (DoctorUser item : doctorUserList) {
            String pinyin = tool.toPinYin(item.getNickname());
            Matcher matcher = pattern.matcher(pinyin);
            if (matcher.find()) {
                item.setNicknameFristChar(("" + pinyin.charAt(0)).toUpperCase());
            } else {
                item.setNicknameFristChar("#");
            }
            boolean exist = false;
            for (int i = 0; i < customers.size(); i++) {
                JSONObject jsonObject = (JSONObject) customers.get(i);
                if (Objects.equals(item.getNicknameFristChar(), jsonObject.get("index"))) {
                    JSONArray array = (JSONArray) jsonObject.get("array");
                    array.add(item);
                    exist = true;
                    break;
                }
            }
            if (!exist) {
                JSONObject customerGroup = new JSONObject();
                customerGroup.put("index", item.getNicknameFristChar());
                JSONArray array = new JSONArray();
                array.add(item);
                customerGroup.put("array", array);
                customers.add(customerGroup);
            }
        }
        customers.sort(new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject o1, JSONObject o2) {
                String o1Val = o1.get("index").toString();
                String o2Val = o2.get("index").toString();
                return o1Val.compareTo(o2Val);
            }

            @Override
            public boolean equals(Object obj) {
                return super.equals(obj);
            }
        });
        return ResponseEntity.ok(customers);
    }

    @PutMapping("/info")
    @ApiOperation(value = "修改鲜果师信息")
    @ApiImplicitParam(paramType = "body", name = "fruitDoctor", value = "要更新的鲜果师成员", required = true, dataType = "FruitDoctor")
    public ResponseEntity update(Sessions.User user, @RequestBody FruitDoctor fruitDoctor) {
        String userId = user.getUser().get("userId").toString();
        FruitDoctor doctors = fruitDoctorService.selectByUserId(Long.valueOf(userId));
        if (Objects.isNull(doctors)) {
            return ResponseEntity.badRequest().body(Tips.warn("鲜果师不存在"));
        }
        fruitDoctor.setId(doctors.getId());
        Tips backMsg = new Tips();
        if (fruitDoctorService.updateById(fruitDoctor) >= 1) {
            backMsg.setCode("1");
            backMsg.setMessage("更新成功");
        }
        //升级为明星鲜果师，发送模板消息
        if ("YES".equals(fruitDoctor.getHot())) {
            FruitDoctor fd = fruitDoctorService.selectById(doctors.getId());
            if (Objects.nonNull(fd)) {
                //不是明星鲜果师的时候才发送模板消息
                if (!"YES".equals(fd.getHot())) {
                    String currentTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    KeywordValue keywordValue = new KeywordValue();
                    keywordValue.setTemplateType(TemplateMessageEnum.UPGRADE_FRUIT_DOCTOR);
                    keywordValue.setKeyword1Value("明星鲜果师");
                    keywordValue.setKeyword2Value("成功");
                    keywordValue.setKeyword3Value(currentTime);
                    keywordValue.setKeyword4Value("如有疑问请致电0731-85240088");
                    keywordValue.setSendToDoctor(false);
                    keywordValue.setUserId(fd.getUserId());
                    //TODO 模板消息由消息总线改成 事件监听
                   /* rabbit.convertAndSend(FruitDoctorOrderExchange.FRUIT_TEMPLATE_MESSAGE.getExchangeName(),
                            FruitDoctorOrderExchange.FRUIT_TEMPLATE_MESSAGE.getQueueName(), Jackson.json(keywordValue));*/
                }
            }
        }
        //如果是升级为明星鲜果师，则发送模板消息
        return ResponseEntity.ok(backMsg);
    }


}
