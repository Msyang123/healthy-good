package com.lhiot.healthygood.service.doctor;

import com.leon.microx.util.BeanUtils;
import com.leon.microx.util.auditing.Random;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.lhiot.healthygood.domain.doctor.RegisterApplication;
import com.lhiot.healthygood.domain.user.FruitDoctor;
import com.lhiot.healthygood.feign.BaseUserServerFeign;
import com.lhiot.healthygood.feign.model.UserDetailResult;
import com.lhiot.healthygood.mapper.doctor.RegisterApplicationMapper;
import com.lhiot.healthygood.mapper.user.FruitDoctorMapper;
import com.lhiot.healthygood.type.*;
import com.lhiot.healthygood.util.DataItem;
import com.lhiot.healthygood.util.DataObject;
import com.lhiot.healthygood.wechat.WeChatUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Description:鲜果师申请记录服务类
 *
 * @author yijun
 * @date 2018/07/26
 */
@Slf4j
@Service
@Transactional
public class RegisterApplicationService {

    private final RegisterApplicationMapper registerApplicationMapper;
    private final FruitDoctorMapper fruitDoctorMapper;
    private final BaseUserServerFeign baseUserServerFeign;
    private WeChatUtil weChatUtil;

    @Autowired
    public RegisterApplicationService(RegisterApplicationMapper registerApplicationMapper, FruitDoctorMapper fruitDoctorMapper,BaseUserServerFeign baseUserServerFeign, WeChatUtil weChatUtil) {
        this.registerApplicationMapper = registerApplicationMapper;
        this.fruitDoctorMapper = fruitDoctorMapper;
        this.baseUserServerFeign = baseUserServerFeign;
        this.weChatUtil = weChatUtil;
    }

    /**
     * Description:新增鲜果师申请记录
     *
     * @param registerApplication
     * @return RegisterApplication
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public RegisterApplication create(RegisterApplication registerApplication) {
        registerApplication.setAuditStatus(AuditStatus.UNAUDITED);
        registerApplication.setCreateAt(new Date());
        this.registerApplicationMapper.create(registerApplication);
        return registerApplication;
    }

    /**
     * Description:根据用户id查找鲜果师最新申请记录
     *
     * @param id
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public RegisterApplication findLastApplicationById(Long id) {
        return this.registerApplicationMapper.findLastApplicationById(id);
    }

    /**
     * 修改鲜果师信息
     *
     * @param fruitDoctor
     * @return
     */
    public Tips updateFruitDoctorInfo(FruitDoctor fruitDoctor) {
        FruitDoctor doctors = fruitDoctorMapper.selectByUserId(fruitDoctor.getUserId());
        if (Objects.isNull(doctors)) {
            return Tips.warn("鲜果师不存在");
        }
        int result = fruitDoctorMapper.updateById(fruitDoctor);
        //升级为明星鲜果师且不是明星鲜果师的时候才发送模板消息
        if (result >= 0 && Objects.equals(Hot.YES, fruitDoctor.getHot()) && Objects.equals(Hot.NO,doctors.getHot())) {
            String currentTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            DataItem dataItem = new DataItem();
            DataObject first = new DataObject();
            DataObject keyword1 = new DataObject();
            DataObject keyword2 = new DataObject();
            DataObject keyword3 = new DataObject();
            DataObject keyword4 = new DataObject();
            DataObject remark = new DataObject();
            first.setValue(FirstAndRemarkData.UPGRADE_FRUIT_DOCTOR.getFirstData());
            keyword1.setValue("明星鲜果师");
            keyword2.setValue("成功");
            keyword3.setValue(currentTime);
            keyword4.setValue("如有疑问请致电0731-85240088");

            dataItem.setFirst(first);
            dataItem.setKeyword1(keyword1);
            dataItem.setKeyword2(keyword2);
            dataItem.setKeyword3(keyword3);
            dataItem.setKeyword4(keyword4);
            dataItem.setRemark(remark);
            weChatUtil.sendMessageToWechat(TemplateMessageEnum.UPGRADE_FRUIT_DOCTOR, fruitDoctor.getOpenId(), dataItem);
        }
        return result > 0 ? Tips.info("修改成功") : Tips.warn("修改失败");
    }

    /**
     * Description:根据id修改鲜果师申请记录
     *
     * @param registerApplication
     * @return
     */
    public Tips updateById(RegisterApplication registerApplication) {
        boolean updated = this.registerApplicationMapper.updateById(registerApplication) > 0;
        if (!updated) {
            return Tips.warn("修改鲜果师申请记录失败");
        }
        // 查询用户openId
        ResponseEntity findBaseUser = baseUserServerFeign.findById(registerApplication.getUserId());
        if (findBaseUser.getStatusCode().isError() || Objects.isNull(findBaseUser.getBody())) {
            return Tips.warn("基础用户服务查询失败");
        }
        UserDetailResult findUserDetailResult = (UserDetailResult) findBaseUser.getBody();

        // 审核通过 新增鲜果师成员记录
        if (Objects.equals(AuditStatus.AGREE, registerApplication.getAuditStatus())) {
            // 幂等添加
            FruitDoctor doctor = fruitDoctorMapper.findFruitDoctorByUserId(registerApplication.getUserId());
            if (Objects.nonNull(doctor)) {
                return Tips.warn("该鲜果师已存在，添加失败");
            }
            // 设置要添加的鲜果师信息
            FruitDoctor fruitDoctor = new FruitDoctor();
            BeanUtils.copyProperties(registerApplication, fruitDoctor);
            fruitDoctor.setRealName(registerApplication.getRealName());
            fruitDoctor.setInviteCode(Random.of(4, Random.Digits._62));
            fruitDoctor.setDoctorLevel(DoctorLevel.TRAINING.toString());
            fruitDoctor.setDoctorStatus(DoctorStatus.VALID);
            fruitDoctor.setCreateAt(Date.from(Instant.now()));
            fruitDoctor.setRefereeId(registerApplication.getRefereeId());
            //查找基础服务对应的微信用户信息
            ResponseEntity<UserDetailResult> userEntity = baseUserServerFeign.findById(registerApplication.getUserId());
            if (userEntity.getStatusCode().isError()) {
                return Tips.warn(userEntity.getBody().toString());
            }
            UserDetailResult userDetailResult = userEntity.getBody();
            //设置头像默认为微信头像
            fruitDoctor.setAvatar(userDetailResult.getAvatar());
            fruitDoctor.setPhoto(userDetailResult.getAvatar());
            fruitDoctor.setUpperbodyPhoto(userDetailResult.getAvatar());
            fruitDoctor.setPhone(registerApplication.getPhone());
            fruitDoctor.setApplicationId(registerApplication.getId());
            fruitDoctor.setUserId(registerApplication.getUserId());
            fruitDoctor.setHot(Hot.NO);
            if (fruitDoctorMapper.create(fruitDoctor) > 0) {
                // 发送模板消息
                this.doctorApplicationSendTemplate(registerApplication.getAuditStatus(), findUserDetailResult.getOpenId());
                return  Tips.info("添加鲜果师成员成功");
            }
            return Tips.warn("添加鲜果师成员失败");
        }
        // 发送模板消息
        this.doctorApplicationSendTemplate(registerApplication.getAuditStatus(), findUserDetailResult.getOpenId());
        return Tips.info("修改成功");
    }

    /**
     * 鲜果师审核发送模板消息
     *
     * @param auditStatus
     * @param openId
     */
    public Tips doctorApplicationSendTemplate(AuditStatus auditStatus, String openId) {
        log.info("===============鲜果师审核发送模板消息:" + auditStatus + ",openId:" + openId);

        String currentTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        DataItem dataItem = new DataItem();
        DataObject first = new DataObject();
        DataObject keyword1 = new DataObject();
        DataObject keyword2 = new DataObject();
        DataObject keyword3 = new DataObject();
        DataObject keyword4 = new DataObject();

        if (AuditStatus.UNAUDITED.equals(auditStatus)) {
            first.setValue(FirstAndRemarkData.APPLY.getFirstData());
            keyword1.setValue(BacklogEnum.APPLICATION.getBacklog());
            keyword4.setValue(BacklogEnum.APPLICATION.getStatus());
            keyword2.setValue(BacklogEnum.APPLICATION.getRemark());
        } else if (AuditStatus.AGREE.equals(auditStatus)) {
            first.setValue(FirstAndRemarkData.APPLY_SUCCESS.getFirstData());
            keyword1.setValue(BacklogEnum.APPLICATION_SUCCESS.getBacklog());
            keyword4.setValue(BacklogEnum.APPLICATION_SUCCESS.getRemark());
            keyword2.setValue(BacklogEnum.APPLICATION_SUCCESS.getStatus());
        } else if (AuditStatus.REJECT.equals(auditStatus)) {
            first.setValue(FirstAndRemarkData.APPLY_FAILURE.getFirstData());
            keyword1.setValue(BacklogEnum.APPLICATION_FAILURE.getBacklog());
            keyword4.setValue(BacklogEnum.APPLICATION_FAILURE.getRemark());
            keyword2.setValue(BacklogEnum.APPLICATION_FAILURE.getStatus());
        }
        keyword3.setValue(currentTime);

        dataItem.setFirst(first);
        dataItem.setKeyword1(keyword1);
        dataItem.setKeyword2(keyword2);
        dataItem.setKeyword3(keyword3);
        dataItem.setKeyword4(keyword4);
        weChatUtil.sendMessageToWechat(TemplateMessageEnum.APPLY_FRUIT_DOCTOR, openId, dataItem);
        return Tips.info("修改成功");
    }

    /**
     * Description: 查询鲜果师申请记录总记录数
     *
     * @param registerApplication
     * @return
     */
    public int count(RegisterApplication registerApplication) {
        return this.registerApplicationMapper.findCount(registerApplication);
    }

    /**
     * Description: 查询鲜果师申请记录分页列表
     *
     * @param registerApplication
     * @return
     */
    public Pages<RegisterApplication> pageList(RegisterApplication registerApplication) {
        List<RegisterApplication> list = registerApplicationMapper.findList(registerApplication);
        boolean pageFlag = Objects.nonNull(registerApplication.getPage()) && Objects.nonNull(registerApplication.getRows()) && registerApplication.getPage() > 0 && registerApplication.getRows() > 0;
        int total = pageFlag ? this.count(registerApplication) : list.size();
        return Pages.of(total, list);
    }
}

