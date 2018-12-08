package com.lhiot.healthygood.service.user;

import com.leon.microx.util.auditing.Random;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.lhiot.healthygood.domain.doctor.RegisterApplication;
import com.lhiot.healthygood.domain.user.DoctorUser;
import com.lhiot.healthygood.domain.user.FruitDoctor;
import com.lhiot.healthygood.event.SendCaptchaSmsEvent;
import com.lhiot.healthygood.feign.BaseUserServerFeign;
import com.lhiot.healthygood.feign.model.UserDetailResult;
import com.lhiot.healthygood.mapper.user.DoctorUserMapper;
import com.lhiot.healthygood.mapper.user.FruitDoctorMapper;
import com.lhiot.healthygood.type.DoctorLevel;
import com.lhiot.healthygood.type.DoctorStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Description:鲜果师成员服务类
 *
 * @author yijun
 * @date 2018/07/26
 */
@Service
@Transactional
@Slf4j
public class FruitDoctorService {

    private final FruitDoctorMapper fruitDoctorMapper;
    private final DoctorUserMapper doctorUserMapper;
    private final ApplicationEventPublisher publisher;
    private final BaseUserServerFeign baseUserServerFeign;


    @Autowired
    public FruitDoctorService(FruitDoctorMapper fruitDoctorMapper, DoctorUserMapper doctorUserMapper, ApplicationEventPublisher publisher, BaseUserServerFeign baseUserServerFeign) {
        this.fruitDoctorMapper = fruitDoctorMapper;
        this.doctorUserMapper = doctorUserMapper;
        this.publisher = publisher;
        this.baseUserServerFeign = baseUserServerFeign;
    }

    /**
     * Description:新增鲜果师成员
     *
     * @param registerApplication
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public Tips create(RegisterApplication registerApplication) {
        // 幂等添加
        FruitDoctor doctor = fruitDoctorMapper.findFruitDoctorByUserId(registerApplication.getUserId());
        if (Objects.nonNull(doctor)) {
            return Tips.warn("该鲜果师已存在，添加失败");
        }
        FruitDoctor fruitDoctor = new FruitDoctor();
        BeanUtils.copyProperties(registerApplication, fruitDoctor);
        this.fruitDoctorMapper.create(fruitDoctor);
        fruitDoctor.setRealName(registerApplication.getRealName());
        fruitDoctor.setInviteCode(Random.of(4, Random.Digits._62));
        fruitDoctor.setDoctorLevel(DoctorLevel.TRAINING.toString());
        fruitDoctor.setDoctorStatus(DoctorStatus.VALID);
        fruitDoctor.setCreateAt(Date.from(Instant.now()));
        //查找推荐人
        DoctorUser doctorUser= doctorUserMapper.selectByUserId(registerApplication.getUserId());
        if(Objects.nonNull(doctorUser)){
            fruitDoctor.setRefereeId(doctorUser.getDoctorId());
        }
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
        return fruitDoctorMapper.create(fruitDoctor) > 0 ? Tips.info("添加鲜果师成员成功") : Tips.warn("添加鲜果师成员失败");
    }

    /**
     * Description:根据id修改鲜果师成员
     *
     * @param fruitDoctor
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public int updateById(FruitDoctor fruitDoctor) {

        // 数据修改

        return this.fruitDoctorMapper.updateById(fruitDoctor);
    }

    /**
     * Description:根据ids删除鲜果师成员
     *
     * @param ids
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public int deleteByIds(String ids) {
        return this.fruitDoctorMapper.deleteByIds(Arrays.asList(ids.split(",")));
    }

    /**
     * Description:根据id查找鲜果师成员
     *
     * @param id
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public FruitDoctor selectById(Long id) {
        return this.fruitDoctorMapper.selectById(id);
    }

    /**
     * Description:根据用户编号查找鲜果师成员
     *
     * @param userId
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public FruitDoctor selectByUserId(Long userId) {
        return this.fruitDoctorMapper.selectByUserId(userId);
    }

    /**
     * Description: 查询鲜果师成员总记录数
     *
     * @param fruitDoctor
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public int count(FruitDoctor fruitDoctor) {
        return this.fruitDoctorMapper.pageFruitDoctorCounts(fruitDoctor);
    }

    /**
     * Description: 查询鲜果师成员分页列表
     *
     * @param fruitDoctor
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public Pages<FruitDoctor> pageList(FruitDoctor fruitDoctor) {
        int total = 0;
        if (fruitDoctor.getRows() != null && fruitDoctor.getRows() > 0) {
            total = this.count(fruitDoctor);
        }
        return Pages.of(total,
                this.fruitDoctorMapper.pageFruitDoctors(fruitDoctor));
    }


    /**
     * Description: 查询鲜果师成员分页列表
     *
     * @param fruitDoctor
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public List<FruitDoctor> list(FruitDoctor fruitDoctor) {
        return this.fruitDoctorMapper.pageFruitDoctors(fruitDoctor);
    }

    /**
     * Description: 查询鲜果师团队分页列表
     *
     * @param fruitDoctor
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public Pages<FruitDoctor> subordinate(FruitDoctor fruitDoctor) {
        int total = 0;
        if (fruitDoctor.getRows() != null && fruitDoctor.getRows() > 0) {
            total = this.count(fruitDoctor);
        }
        return Pages.of(total,
                this.fruitDoctorMapper.subordinate(fruitDoctor));
    }

    /**
     * 根据邀请码获取鲜果师
     *
     * @param inviteCode
     * @return
     */
    public FruitDoctor findDoctorByInviteCode(String inviteCode) {
        return fruitDoctorMapper.selectByInviteCode(inviteCode);
    }


    /**
     * Description: 根据用户id查询上级鲜果师信息
     *
     * @param userId
     * @return
     * @author hufan
     * @date 2018/08/21 12:08:13
     */
    public FruitDoctor findSuperiorFruitDoctorByUserId(Long userId) {
        return fruitDoctorMapper.findSuperiorFruitDoctorByUserId(userId);
    }


    /**
     * 鲜果师注册时绑定手机号码，发送模板消息
     *
     * @param phone 待发送验证码手机号
     */
    public void bandPhoneSendTemplateMessage(String phone) {

        //发送模板消息
        publisher.publishEvent(new SendCaptchaSmsEvent(phone));

        log.info("鲜果师注册时绑定手机号码，发送模板消息");
    }

}

