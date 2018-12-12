package com.lhiot.healthygood.mapper.user;

import com.lhiot.healthygood.domain.user.DoctorCustomer;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
* Description:鲜果师客户Mapper类
* @author yijun
* @date 2018/07/26
*/
@Mapper
@Repository
public interface DoctorCustomerMapper {

    /**
    * Description:新增鲜果师客户
    *
    * @param DoctorCustomer
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */
    int create(DoctorCustomer DoctorCustomer);

    /**
    * Description:根据id修改鲜果师客户
    *
    * @param DoctorCustomer
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */
    int updateById(DoctorCustomer DoctorCustomer);

    /**
     * Description:鲜果师修改用户备注
     *
     * @param DoctorCustomer
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    int updateRemarkName(DoctorCustomer DoctorCustomer);

    /**
    * Description:根据ids删除鲜果师客户
    *
    * @param ids
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */
    int deleteByIds(List<String> ids);

    /**
    * Description:根据用户编号查找鲜果师客户中关联的鲜果师
    *
    * @param userId
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */
    DoctorCustomer selectByUserId(Long userId);

    /**
     * Description:根据id查找鲜果师客户
     *
     * @param id
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    DoctorCustomer selectById(Long id);

    List<DoctorCustomer> selectByDoctorId(Long doctorId);

    /**
    * Description:查询鲜果师客户列表
    *
    * @param DoctorCustomer
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */
     List<DoctorCustomer> pageDoctorCustomers(DoctorCustomer DoctorCustomer);


    /**
    * Description: 查询鲜果师客户总记录数
    *
    * @param DoctorCustomer
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */
    int pageDoctorCustomerCounts(DoctorCustomer DoctorCustomer);



    /**
     * Description:查询鲜果师客户列表
     *
     * @param id
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    List<DoctorCustomer> doctorCustomers(Long id);
}
