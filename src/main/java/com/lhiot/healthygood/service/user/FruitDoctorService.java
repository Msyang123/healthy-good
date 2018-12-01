package com.lhiot.healthygood.service.user;

import com.leon.microx.web.result.Pages;
import com.lhiot.healthygood.domain.user.FruitDoctor;
import com.lhiot.healthygood.event.SendCaptchaSmsEvent;
import com.lhiot.healthygood.mapper.user.FruitDoctorMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
* Description:鲜果师成员服务类
* @author yijun
* @date 2018/07/26
*/
@Service
@Transactional
@Slf4j
public class FruitDoctorService {

    private final FruitDoctorMapper fruitDoctorMapper;

    private final ApplicationEventPublisher publisher;

    @Autowired
    public FruitDoctorService(FruitDoctorMapper fruitDoctorMapper, ApplicationEventPublisher publisher) {
        this.fruitDoctorMapper = fruitDoctorMapper;
        this.publisher = publisher;
    }

    /** 
    * Description:新增鲜果师成员
    *  
    * @param fruitDoctor
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */  
    public int create(FruitDoctor fruitDoctor){
        return this.fruitDoctorMapper.create(fruitDoctor);
    }

    /** 
    * Description:根据id修改鲜果师成员
    *  
    * @param fruitDoctor
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */ 
    public int updateById(FruitDoctor fruitDoctor){
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
    public int deleteByIds(String ids){
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
    public FruitDoctor selectById(Long id){
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
    public FruitDoctor selectByUserId(Long userId){
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
    public int count(FruitDoctor fruitDoctor){
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
    public List<FruitDoctor> list(FruitDoctor fruitDoctor){
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
    public Pages<FruitDoctor> subordinate(FruitDoctor fruitDoctor){
        int total = 0;
        if (fruitDoctor.getRows() != null && fruitDoctor.getRows() > 0) {
            total = this.count(fruitDoctor);
        }
        return Pages.of(total,
                this.fruitDoctorMapper.subordinate(fruitDoctor));
    }
    
    /**
     * 根据邀请码获取鲜果师
     * @param inviteCode
     * @return
     */
    public FruitDoctor findDoctorByInviteCode(String inviteCode){
    	return fruitDoctorMapper.selectByInviteCode(inviteCode);
    }


    /**
     * Description: 根据用户id查询上级鲜果师信息
     * @param userId
     * @return
     * @author hufan
     * @date 2018/08/21 12:08:13
     */
    public FruitDoctor findSuperiorFruitDoctorByUserId(Long userId){
        return fruitDoctorMapper.findSuperiorFruitDoctorByUserId(userId);
    }


    /**
     * 鲜果师注册时绑定手机号码，发送模板消息
     * @param phone 待发送验证码手机号
     */
    public void bandPhoneSendTemplateMessage(String phone) {

        //发送模板消息
        publisher.publishEvent(new SendCaptchaSmsEvent(phone));

        log.info("鲜果师注册时绑定手机号码，发送模板消息");
    }

}

