package com.lhiot.healthygood.service.user;

import com.leon.microx.web.result.Pages;
import com.lhiot.healthygood.domain.user.DoctorUser;
import com.lhiot.healthygood.mapper.user.DoctorUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
* Description:鲜果师客户服务类
* @author yijun
* @date 2018/07/26
*/
@Service
@Transactional
public class DoctorUserService {

    private final DoctorUserMapper doctorUserMapper;

    @Autowired
    public DoctorUserService(DoctorUserMapper doctorUserMapper) {
        this.doctorUserMapper = doctorUserMapper;
    }

    /** 
    * Description:新增鲜果师客户
    *  
    * @param doctorUser
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */  
    public int create(DoctorUser doctorUser){
        return this.doctorUserMapper.create(doctorUser);
    }

    /** 
    * Description:根据id修改鲜果师客户
    *  
    * @param doctorUser
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */ 
    public int updateById(DoctorUser doctorUser){
        return this.doctorUserMapper.updateById(doctorUser);
    }

    /**
     * Description:鲜果师修改用户备注
     *
     * @param doctorUser
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public int updateRemarkName(DoctorUser doctorUser){
        return this.doctorUserMapper.updateRemarkName(doctorUser);
    }


    /** 
    * Description:根据ids删除鲜果师客户
    *  
    * @param ids
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */ 
    public int deleteByIds(String ids){
        return this.doctorUserMapper.deleteByIds(Arrays.asList(ids.split(",")));
    }
    
    /** 
    * Description:根据id查找鲜果师客户
    *  
    * @param id
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */ 
    public DoctorUser selectById(Long id){
        return this.doctorUserMapper.selectById(id);
    }

    /**
     * Description:根据用户编号查找鲜果师客户
     *
     * @param userId
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public DoctorUser selectByUserId(Long userId){
        return this.doctorUserMapper.selectByUserId(userId);
    }

    public DoctorUser selectByDoctorId(Long doctorId){
        return this.doctorUserMapper.selectByDoctorId(doctorId);
    }

    /** 
    * Description: 查询鲜果师客户总记录数
    *  
    * @param doctorUser
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */  
    public int count(DoctorUser doctorUser){
        return this.doctorUserMapper.pageDoctorUserCounts(doctorUser);
    }
    
    /** 
    * Description: 查询鲜果师客户分页列表
    *  
    * @param doctorUser
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */  
    public Pages<DoctorUser> pageList(DoctorUser doctorUser) {
       int total = 0;
       if (doctorUser.getRows() != null && doctorUser.getRows() > 0) {
           total = this.count(doctorUser);
       }
       return Pages.of(total,
              this.doctorUserMapper.pageDoctorUsers(doctorUser));
    }

    /**
     * Description: 查询鲜果师客户列表
     *
     * @param id
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public List<DoctorUser> doctorCustomers(Long id) {

        return this.doctorUserMapper.doctorCustomers(id);
    }
}

