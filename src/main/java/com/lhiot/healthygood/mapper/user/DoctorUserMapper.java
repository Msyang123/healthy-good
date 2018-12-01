package com.lhiot.healthygood.mapper.user;

import com.lhiot.healthygood.domain.user.DoctorUser;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* Description:鲜果师客户Mapper类
* @author yijun
* @date 2018/07/26
*/
@Mapper
public interface DoctorUserMapper {

    /**
    * Description:新增鲜果师客户
    *
    * @param doctorUser
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */
    int create(DoctorUser doctorUser);

    /**
    * Description:根据id修改鲜果师客户
    *
    * @param doctorUser
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */
    int updateById(DoctorUser doctorUser);

    /**
     * Description:鲜果师修改用户备注
     *
     * @param doctorUser
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    int updateRemarkName(DoctorUser doctorUser);

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
    DoctorUser selectByUserId(Long userId);

    /**
     * Description:根据id查找鲜果师客户
     *
     * @param id
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    DoctorUser selectById(Long id);

    DoctorUser selectByDoctorId(Long doctorId);

    /**
    * Description:查询鲜果师客户列表
    *
    * @param doctorUser
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */
     List<DoctorUser> pageDoctorUsers(DoctorUser doctorUser);


    /**
    * Description: 查询鲜果师客户总记录数
    *
    * @param doctorUser
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */
    int pageDoctorUserCounts(DoctorUser doctorUser);



    /**
     * Description:查询鲜果师客户列表
     *
     * @param id
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    List<DoctorUser> doctorCustomers(Long id);
}
