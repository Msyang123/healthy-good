package com.lhiot.healthygood.service.user;

import com.leon.microx.web.result.Pages;
import com.lhiot.healthygood.domain.user.DoctorCustomer;
import com.lhiot.healthygood.domain.user.DoctorCustomer;
import com.lhiot.healthygood.mapper.user.DoctorCustomerMapper;
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
public class DoctorCustomerService {

    private final DoctorCustomerMapper DoctorCustomerMapper;

    @Autowired
    public DoctorCustomerService(DoctorCustomerMapper DoctorCustomerMapper) {
        this.DoctorCustomerMapper = DoctorCustomerMapper;
    }

    /** 
    * Description:新增鲜果师客户
    *  
    * @param DoctorCustomer
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */  
    public int create(DoctorCustomer DoctorCustomer){
        return this.DoctorCustomerMapper.create(DoctorCustomer);
    }

    /** 
    * Description:根据id修改鲜果师客户
    *  
    * @param DoctorCustomer
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */ 
    public int updateById(DoctorCustomer DoctorCustomer){
        return this.DoctorCustomerMapper.updateById(DoctorCustomer);
    }

    /**
     * Description:鲜果师修改用户备注
     *
     * @param DoctorCustomer
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public int updateRemarkName(DoctorCustomer DoctorCustomer){
        return this.DoctorCustomerMapper.updateRemarkName(DoctorCustomer);
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
        return this.DoctorCustomerMapper.deleteByIds(Arrays.asList(ids.split(",")));
    }
    
    /** 
    * Description:根据id查找鲜果师客户
    *  
    * @param id
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */ 
    public DoctorCustomer selectById(Long id){
        return this.DoctorCustomerMapper.selectById(id);
    }

    /**
     * Description:根据用户编号查找鲜果师客户
     *
     * @param userId
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public DoctorCustomer selectByUserId(Long userId){
        return this.DoctorCustomerMapper.selectByUserId(userId);
    }

    public List<DoctorCustomer> selectByDoctorId(Long doctorId){
        return this.DoctorCustomerMapper.selectByDoctorId(doctorId);
    }

    /** 
    * Description: 查询鲜果师客户总记录数
    *  
    * @param DoctorCustomer
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */  
    public int count(DoctorCustomer DoctorCustomer){
        return this.DoctorCustomerMapper.pageDoctorCustomerCounts(DoctorCustomer);
    }
    
    /** 
    * Description: 查询鲜果师客户分页列表
    *  
    * @param DoctorCustomer
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */  
    public Pages<DoctorCustomer> pageList(DoctorCustomer DoctorCustomer) {
       int total = 0;
       if (DoctorCustomer.getRows() != null && DoctorCustomer.getRows() > 0) {
           total = this.count(DoctorCustomer);
       }
       return Pages.of(total,
              this.DoctorCustomerMapper.pageDoctorCustomers(DoctorCustomer));
    }

    /**
     * Description: 查询鲜果师客户列表
     *
     * @param id
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public List<DoctorCustomer> doctorCustomers(Long id) {

        return this.DoctorCustomerMapper.doctorCustomers(id);
    }
}

