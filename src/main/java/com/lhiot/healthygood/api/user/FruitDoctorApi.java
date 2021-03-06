package com.lhiot.healthygood.api.user;

import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.leon.microx.web.session.Sessions;
import com.leon.microx.web.swagger.ApiParamType;
import com.lhiot.healthygood.domain.doctor.*;
import com.lhiot.healthygood.domain.user.DoctorCustomer;
import com.lhiot.healthygood.domain.user.FruitDoctor;
import com.lhiot.healthygood.domain.user.ValidateParam;
import com.lhiot.healthygood.feign.BaseUserServerFeign;
import com.lhiot.healthygood.feign.OrderServiceFeign;
import com.lhiot.healthygood.feign.ThirdpartyServerFeign;
import com.lhiot.healthygood.feign.model.OrderDetailResult;
import com.lhiot.healthygood.feign.model.UserDetailResult;
import com.lhiot.healthygood.service.doctor.CardUpdateLogService;
import com.lhiot.healthygood.service.doctor.DoctorAchievementLogService;
import com.lhiot.healthygood.service.doctor.RegisterApplicationService;
import com.lhiot.healthygood.service.doctor.SettlementApplicationService;
import com.lhiot.healthygood.service.user.DoctorCustomerService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
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
    private final DoctorCustomerService doctorCustomerService;
    private final FruitDoctorService fruitDoctorService;
    private final DoctorAchievementLogService doctorAchievementLogService;
    private final CardUpdateLogService cardUpdateLogService;
    private final BaseUserServerFeign baseUserServerFeign;
    private final OrderServiceFeign orderServiceFeign;

    @Autowired
    public FruitDoctorApi(ThirdpartyServerFeign thirdpartyServerFeign, RegisterApplicationService registerApplicationService, SettlementApplicationService settlementApplicationService,
                          DoctorCustomerService doctorCustomerService, DoctorCustomerService doctorCustomerService1, FruitDoctorService fruitDoctorService, DoctorAchievementLogService doctorAchievementLogService, CardUpdateLogService cardUpdateLogService, BaseUserServerFeign baseUserServerFeign, OrderServiceFeign orderServiceFeign) {
        this.thirdpartyServerFeign = thirdpartyServerFeign;
        this.registerApplicationService = registerApplicationService;
        this.settlementApplicationService = settlementApplicationService;
        this.doctorCustomerService = doctorCustomerService1;
        this.fruitDoctorService = fruitDoctorService;
        this.doctorAchievementLogService = doctorAchievementLogService;
        this.cardUpdateLogService = cardUpdateLogService;
        this.baseUserServerFeign = baseUserServerFeign;
        this.orderServiceFeign = orderServiceFeign;
    }

    @Sessions.Uncheck
    @GetMapping("/sms/captcha")
    @ApiOperation(value = "发送鲜果师申请验证码")
    @ApiImplicitParam(paramType = "query", name = "phone", value = "发送鲜果师申请验证码对应手机号", required = true, dataType = "String")
    public ResponseEntity captcha(@RequestParam String phone) {

        fruitDoctorService.bandPhoneSendTemplateMessage(phone);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/qualifications")
    @ApiOperation(value = "添加鲜果师申请记录*", response = RegisterApplication.class)
    @ApiImplicitParam(paramType = "body", name = "registerApplication", value = "要添加的鲜果师申请记录", required = true, dataType = "RegisterApplication")
    public ResponseEntity qualifications(Sessions.User user, @RequestBody RegisterApplication registerApplication) {
        log.debug("添加鲜果师申请记录\t param:{}", registerApplication);
        String userId = user.getUser().get("userId").toString();
        registerApplication.setUserId(Long.valueOf(userId));

        ValidateParam smsValidateParam = new ValidateParam();
        smsValidateParam.setCode(registerApplication.getVerificationCode());
        smsValidateParam.setPhoneNumber(registerApplication.getPhone());
        ResponseEntity responseEntity = thirdpartyServerFeign.validate(CaptchaTemplate.REGISTER, smsValidateParam);
        if (Objects.isNull(responseEntity) || responseEntity.getStatusCode().isError()) {
            return responseEntity;
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
    public ResponseEntity update(@PathVariable("id") Long id, @RequestBody RegisterApplication registerApplication) {
        log.debug("根据id更新鲜果师申请记录\t id:{} param:{}", id, registerApplication);

        RegisterApplication findLastApplication = registerApplicationService.findLastApplicationById(registerApplication.getUserId());
        if (Objects.isNull(findLastApplication)) {
            return ResponseEntity.badRequest().body("未找到该用户审核记录");
        }
        registerApplication.setId(id);
        registerApplication.setAuditAt(Date.from(Instant.now()));
        Tips tips = registerApplicationService.updateById(registerApplication);
        if (tips.err()) {
            return ResponseEntity.badRequest().body(tips.getMessage());
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
    @ApiOperation(value = "申请提现红利*")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "amount", value = "申请提现红利", required = true, dataType = "int")
    })
    public ResponseEntity create(Sessions.User user,
                                 @RequestParam int amount) {
        log.debug("申请提现红利\t param:{}", amount);
        String userId = user.getUser().get("userId").toString();
        FruitDoctor fruitDoctor = fruitDoctorService.selectByUserId(Long.valueOf(userId));
        if (Objects.isNull(fruitDoctor)) {
            return ResponseEntity.badRequest().body("鲜果师不存在");
        }
        //如果不是系统的每月
        LocalDate localDate = LocalDate.now();
        //每月15号
        LocalDate fifteen = LocalDate.of(localDate.getYear(), localDate.getMonth(), 15);
        //每月17号
        LocalDate seventeen = LocalDate.of(localDate.getYear(), localDate.getMonth(), 17);
        /*if (localDate.isBefore(fifteen) || localDate.isAfter(seventeen)) {
            return ResponseEntity.badRequest().body("不在每月15-17日申请时间段内");
        }*/
        //查询申请金额是否超过实际金额
        if (Objects.isNull(fruitDoctor.getSettlement()) || fruitDoctor.getSettlement() < amount) {
            return ResponseEntity.badRequest().body("申请金额超过可结算余额");
        }
        SettlementApplication settlementApplication = new SettlementApplication();
        settlementApplication.setAmount(amount);
        settlementApplication.setDoctorId(fruitDoctor.getId());
        settlementApplication.setCreateAt(Date.from(Instant.now()));
        settlementApplication.setSettlementStatus(SettlementStatus.UNSETTLED);
        settlementApplication.setOpenId(user.getUser().get("openId").toString());
        int result = settlementApplicationService.settlement(settlementApplication);
        return result > 0 ? ResponseEntity.ok("申请成功！") : ResponseEntity.badRequest().body("申请失败！");
    }

    @Sessions.Uncheck
    @ApiOperation(value = "结算申请修改(后台)")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = ApiParamType.PATH, name = "id", value = "结算申请id", required = true, dataType = "Long"),
            @ApiImplicitParam(paramType = ApiParamType.BODY, name = "settlementApplication", value = "要修改的结算申请信息", dataType = "SettlementApplication", required = true)
    })
    @PutMapping("/settlement/{id}")
    public ResponseEntity updateSettlement(@PathVariable("id") Long id, @RequestBody SettlementApplication settlementApplication) {
        log.debug("结算申请修改\t id:{},param:{}", id, settlementApplication);

        Tips tips = settlementApplicationService.updateById(id, settlementApplication);
        return tips.err() ? ResponseEntity.badRequest().body(tips.getMessage()) : ResponseEntity.ok().build();
    }

    @Sessions.Uncheck
    @ApiOperation(value = "结算申请退款(后台)")
    @ApiImplicitParam(paramType = ApiParamType.PATH, name = "id", value = "结算申请id", required = true, dataType = "Long")
    @PutMapping("/settlement/refund/{id}")
    public ResponseEntity refund(@PathVariable("id") Long id) {
        log.debug("结算申请退款\t id:{},param:{}", id);

        Tips tips = settlementApplicationService.refund(id);
        return tips.err() ? ResponseEntity.badRequest().body(tips.getMessage()) : ResponseEntity.ok().build();
    }

    @Sessions.Uncheck
    @ApiOperation(value = "结算申请分页查询(后台)", response = SettlementApplication.class, responseContainer = "Set")
    @ApiImplicitParam(paramType = ApiParamType.BODY, name = "settlementApplication", value = "结算申请分页查询条件", dataType = "SettlementApplication", required = true)
    @PostMapping("/settlement/pages")
    public ResponseEntity search(@RequestBody SettlementApplication settlementApplication) {
        log.debug("结算申请分页查询\t param:{}", settlementApplication);

        Pages<SettlementApplication> settlementApplicationPages = settlementApplicationService.pageList(settlementApplication);
        return ResponseEntity.ok(settlementApplicationPages);
    }

    @PutMapping("/remark")
    @ApiOperation(value = "鲜果师修改用户备注*")
    @ApiImplicitParam(paramType = "body", name = "DoctorCustomer", value = "鲜果师修改用户备注实体信息", required = true, dataType = "DoctorCustomer")
    public ResponseEntity updateRemarkName(Sessions.User user, @RequestBody DoctorCustomer doctorCustomer) {
        String userId = user.getUser().get("userId").toString();
        FruitDoctor fruitDoctor = fruitDoctorService.selectByUserId(Long.valueOf(userId));
        if (Objects.isNull(fruitDoctor)) {
            return ResponseEntity.badRequest().body("鲜果师不存在");
        }
        log.debug("鲜果师修改用户备注\t param:{}", doctorCustomer);
        doctorCustomer.setDoctorId(fruitDoctor.getId());
        int result = doctorCustomerService.updateRemarkName(doctorCustomer);
        return result > 0 ? ResponseEntity.ok("更新成功") : ResponseEntity.badRequest().body("更新失败");
    }


    @GetMapping("/subordinate")
    @ApiOperation(value = "团队列表*", response = FruitDoctor.class, responseContainer = "Set")
    public ResponseEntity team(Sessions.User user) {
        String userId = user.getUser().get("userId").toString();
        FruitDoctor fruitDoctor = fruitDoctorService.selectByUserId(Long.valueOf(userId));
        if (Objects.isNull(fruitDoctor)) {
            return ResponseEntity.badRequest().body("鲜果师不存在");
        }
        FruitDoctor doctor = new FruitDoctor();
        doctor.setRefereeId(fruitDoctor.getId());
        return ResponseEntity.ok(fruitDoctorService.subordinate(doctor));
    }

    @GetMapping("/page")
    @ApiOperation(value = "查询鲜果师成员分页列表*", response = FruitDoctor.class, responseContainer = "Set")
    public ResponseEntity pageQuery(Sessions.User user) {
        String userId = user.getUser().get("userId").toString();
        FruitDoctor fruitDoctor = fruitDoctorService.selectByUserId(Long.valueOf(userId));
        if (Objects.isNull(fruitDoctor)) {
            return ResponseEntity.badRequest().body("鲜果师不存在");
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

        Pages<FruitDoctor> fruitDoctorPages = fruitDoctorService.pageList(fruitDoctor);
        return ResponseEntity.ok(fruitDoctorPages);
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
        fruitDoctor.getUserId();
        ResponseEntity findBaseUser = baseUserServerFeign.findById(fruitDoctor.getUserId());
        if (findBaseUser.getStatusCode().isError() || Objects.isNull(findBaseUser.getBody())) {
            return ResponseEntity.badRequest().body("基础服务查询失败");
        }
        UserDetailResult userDetailResult = (UserDetailResult) findBaseUser.getBody();
        fruitDoctor.setOpenId(userDetailResult.getOpenId());
        Tips tips = registerApplicationService.updateFruitDoctorInfo(fruitDoctor);
        return tips.err() ? ResponseEntity.badRequest().body(tips.getMessage()) : ResponseEntity.ok().body(tips.getMessage());
    }

    @GetMapping("/incomes/detail")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "page", dataType = "int", required = true, value = "多少页"),
            @ApiImplicitParam(paramType = "query", name = "rows", dataType = "int", required = true, value = "数据多少条"),
            @ApiImplicitParam(paramType = "query", name = "incomeType", dataTypeClass = IncomeType.class, required = true, value = "收入支出类型")
    })
    @ApiOperation(value = "收支明细*", response = DoctorAchievementLog.class, responseContainer = "Set")
    public ResponseEntity pageQuery(Sessions.User user, @RequestParam IncomeType incomeType, @RequestParam Integer page, @RequestParam Integer rows) {
        String userId = user.getUser().get("userId").toString();
        FruitDoctor fruitDoctor = fruitDoctorService.selectByUserId(Long.valueOf(userId));
        if (Objects.isNull(fruitDoctor)) {
            return ResponseEntity.badRequest().body("鲜果师不存在");
        }
        DoctorAchievementLog doctorAchievementLog = new DoctorAchievementLog();
        doctorAchievementLog.setDoctorId(fruitDoctor.getId());
        doctorAchievementLog.setIncomeType(incomeType);
        doctorAchievementLog.setRows(rows);
        doctorAchievementLog.setPage(page);
        return ResponseEntity.ok(doctorAchievementLogService.pageList(doctorAchievementLog));
    }

    @GetMapping("/incomes")
    @ApiOperation(value = "我的收入*", response = IncomeStat.class, responseContainer = "Set")
    public ResponseEntity myIncome(Sessions.User user) {
        String userId = user.getUser().get("userId").toString();
        FruitDoctor fruitDoctor = fruitDoctorService.selectByUserId(Long.valueOf(userId));
        if (Objects.isNull(fruitDoctor)) {
            return ResponseEntity.badRequest().body("鲜果师不存在");
        }
        IncomeStat incomeStat = doctorAchievementLogService.myIncome(fruitDoctor.getId());
        if (Objects.nonNull(incomeStat)) {
            incomeStat.setBonus(fruitDoctor.getBonus());
            incomeStat.setBonusCanBeSettled(fruitDoctor.getSettlement());
        }
        return ResponseEntity.ok(incomeStat);
    }

    @Sessions.Uncheck
    @GetMapping("/member-achievements/{doctorId}")
    @ApiOperation(value = "我的团队的个人业绩", response = TeamAchievement.class, responseContainer = "Set")
    @ApiImplicitParam(paramType = "path", name = "doctorId", value = "鲜果师id", required = true, dataType = "Long")
    public ResponseEntity teamAchievement(@PathVariable Long doctorId) {
        return ResponseEntity.ok(doctorAchievementLogService.teamAchievement(doctorId));
    }

    @ApiOperation(value = "统计本期和上期的日周月季度的业绩*", response = Achievement.class, responseContainer = "Set")
    @ApiImplicitParam(paramType = "query", name = "dateType", dataTypeClass = DateTypeEnum.class, required = true, value = "统计的类型")
    @GetMapping("/achievement/period")
    public ResponseEntity findAchievement(Sessions.User user, @RequestParam DateTypeEnum dateType) {
        String userId = user.getUser().get("userId").toString();
        FruitDoctor fruitDoctor = fruitDoctorService.selectByUserId(Long.valueOf(userId));
        if (Objects.isNull(fruitDoctor)) {
            return ResponseEntity.badRequest().body("鲜果师不存在");
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

    @ApiOperation(value = "统计今日业绩订单数及总的业绩*", response = Achievement.class, responseContainer = "Set")
    @GetMapping("/achievement")
    public ResponseEntity findTodayAchievement(Sessions.User user) {
        String userId = user.getUser().get("userId").toString();
        FruitDoctor fruitDoctor = fruitDoctorService.selectByUserId(Long.valueOf(userId));
        if (Objects.isNull(fruitDoctor)) {
            return ResponseEntity.badRequest().body("鲜果师不存在");
        }
        //统计今天订单数和业绩
        Achievement current =
                doctorAchievementLogService.achievement(DateTypeEnum.DAY, PeriodType.current, fruitDoctor.getId(), true, false, null);
        //统计总的订单数
        Achievement total =
                doctorAchievementLogService.achievement(DateTypeEnum.DAY, PeriodType.current, fruitDoctor.getId(), true, true, null);
        //构建返回值
       /* Long count  = doctorAchievementLogService.achievementTodayOrderCount(DateTypeEnum.DAY, PeriodType.current, fruitDoctor.getId());
        current.setOrderCount(count);*/
        current.setSalesToday(current.getSalesAmount());//今日销售额
        current.setSalesAmount(total.getSalesAmount());//总销售额
        ResponseEntity userInfo = baseUserServerFeign.findById(Long.valueOf(userId));
        if (userInfo.getStatusCode().isError()) {
            return ResponseEntity.badRequest().body(userInfo.getBody());
        }
        UserDetailResult users = (UserDetailResult) userInfo.getBody();
        FruitDoctor doctor = fruitDoctorService.selectByUserId(users.getId());
        if (Objects.nonNull(doctor)) {
            users.setFruitDoctor(true);
            users.setFruitDoctorInfo(doctor);
        }
        current.setUserDetailResult(users);
        return ResponseEntity.ok(current);
    }

    @PostMapping("/bank-card")
    @ApiOperation(value = "银行卡添加/修改*")
    @ApiImplicitParam(paramType = "body", name = "cardUpdateLog", value = "要添加的", required = true, dataType = "CardUpdateLog")
    public ResponseEntity create(Sessions.User user, @RequestBody CardUpdateLog cardUpdateLog) {
        log.debug("添加\t param:{}", cardUpdateLog);
        String userId = user.getUser().get("userId").toString();
        FruitDoctor fruitDoctor = fruitDoctorService.selectByUserId(Long.valueOf(userId));
        if (Objects.isNull(fruitDoctor)) {
            return ResponseEntity.badRequest().body("鲜果师不存在");
        }
        CardUpdateLog cardParam = new CardUpdateLog();
        cardParam.setDoctorId(fruitDoctor.getId());
        cardUpdateLog.setDoctorId(fruitDoctor.getId());
        cardUpdateLog.setUpdateAt(Date.from(Instant.now()));
        cardUpdateLog.setCardUsername(fruitDoctor.getRealName());
        return cardUpdateLogService.create(cardUpdateLog) ? ResponseEntity.ok("操作成功") : ResponseEntity.badRequest().body("操作失败");
    }

    @ApiOperation(value = "查询鲜果师银行卡信息*", notes = "根据session里的doctorId查询", response = CardUpdateLog.class)
    @GetMapping("/bank-card")
    public ResponseEntity findCardUpdateLog(Sessions.User user) {
        String userId = user.getUser().get("userId").toString();
        FruitDoctor fruitDoctor = fruitDoctorService.selectByUserId(Long.valueOf(userId));
        if (Objects.isNull(fruitDoctor)) {
            return ResponseEntity.badRequest().body("鲜果师不存在");
        }
        CardUpdateLog cardUpdateLog = new CardUpdateLog();
        cardUpdateLog.setDoctorId(fruitDoctor.getId());
        CardUpdateLog log = cardUpdateLogService.selectByCard(cardUpdateLog);
        return Objects.isNull(log) ? ResponseEntity.ok().body(new CardUpdateLog()) : ResponseEntity.ok().body(log);
    }

    @ApiOperation(value = "查询鲜果师最新申请记录*", notes = "查询鲜果师最新申请记录", response = RegisterApplication.class)
    @GetMapping("/new-application")
    public ResponseEntity<RegisterApplication> findLastApplicationById(Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        return ResponseEntity.ok(registerApplicationService.findLastApplicationById(userId));
    }

    @GetMapping("/customers")
    @ApiOperation(value = "查询鲜果师客户列表*", response = DoctorCustomer.class, responseContainer = "List")
    public ResponseEntity doctorCustomers(Sessions.User user) {
        String userId = user.getUser().get("userId").toString();
        FruitDoctor fruitDoctor = fruitDoctorService.selectByUserId(Long.valueOf(userId));
        if (Objects.isNull(fruitDoctor)) {
            return ResponseEntity.badRequest().body("鲜果师不存在");
        }
        List<DoctorCustomer> doctorCustomerList = doctorCustomerService.doctorCustomers(fruitDoctor.getId());
        doctorCustomerList.stream().forEach(doctorCustomer -> {
            ResponseEntity userInfoEntity = baseUserServerFeign.findById(doctorCustomer.getUserId());
            if (userInfoEntity.getStatusCode().isError()) {
                return;
            }
            UserDetailResult userDetailResult = (UserDetailResult) userInfoEntity.getBody();
            doctorCustomer.setNickname(userDetailResult.getNickname());
            doctorCustomer.setAvatar(userDetailResult.getAvatar());
            doctorCustomer.setPhone(userDetailResult.getPhone());
        });
        PinyinTool tool = new PinyinTool();
        Pattern pattern = Pattern.compile("^[a-zA-Z]");

        JSONArray customers = new JSONArray();
        doctorCustomerList.stream().filter(Objects::nonNull).forEach(item -> {
            String pinyin = null;
            try {
                pinyin = tool.toPinYin(item.getNickname());
            } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
                badHanyuPinyinOutputFormatCombination.printStackTrace();
            }
            Matcher matcher = pattern.matcher(pinyin);

            item.setNicknameFristChar(matcher.find() ? ("" + pinyin.charAt(0)).toUpperCase() : "#");
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
        });
        customers.sort(new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject o1, JSONObject o2) {
                String o1Val = o1.get("index").toString();
                String o2Val = o2.get("index").toString();
                return o1Val.compareTo(o2Val);
            }
        });
        return ResponseEntity.ok(customers);
    }

    @Sessions.Uncheck
    @PostMapping("/bonus")
    @ApiOperation(value = "每月10号定时结算鲜果师红利")
    public ResponseEntity calculationFruitDoctorBonus() {
        FruitDoctor param = new FruitDoctor();
        param.setDoctorStatus(DoctorStatus.VALID);
        List<FruitDoctor> fruitDoctors = fruitDoctorService.list(param);
        fruitDoctors.forEach(fruitDoctor -> {
            DoctorAchievementLog logParam = new DoctorAchievementLog();
            logParam.setDoctorId(fruitDoctor.getId());
            logParam.setSettlement("true");
            logParam.setSourceType(SourceType.SUB_DISTRIBUTOR);
            if (doctorAchievementLogService.doctorAchievementLogCounts(logParam) > 0) {
                log.info(fruitDoctor.getRealName() + fruitDoctor.getId() + "已执行过");
                return;
            }
            doctorAchievementLogService.bounsSettlement(fruitDoctor.getId());
        });
        return ResponseEntity.ok().build();
    }

    @Sessions.Uncheck
    @PostMapping("/ceshi/commission")
    @ApiOperation(value = "测试鲜果师订单分成接口")
    @ApiImplicitParam(paramType = "query", name = "code", dataType = "String", required = true, value = "订单编号")
    public ResponseEntity ceshi(@RequestParam String code) {
        ResponseEntity orderDetailResult = orderServiceFeign.orderDetail(code, false, false);
        OrderDetailResult order = (OrderDetailResult) orderDetailResult.getBody();
        fruitDoctorService.calculationCommission(order.getCode());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/info")
    @ApiOperation(value = "修改鲜果师信息*")
    @ApiImplicitParam(paramType = "body", name = "fruitDoctor", value = "要更新的鲜果师成员", required = true, dataType = "FruitDoctor")
    public ResponseEntity update(Sessions.User user, @RequestBody FruitDoctor fruitDoctor) {
        String userId = user.getUser().get("userId").toString();
        fruitDoctor.setUserId(Long.valueOf(userId));
        Tips tips = registerApplicationService.updateFruitDoctorInfo(fruitDoctor);
        return tips.err() ? ResponseEntity.badRequest().body(tips.getMessage()) : ResponseEntity.ok().body(tips.getMessage());
    }


}
