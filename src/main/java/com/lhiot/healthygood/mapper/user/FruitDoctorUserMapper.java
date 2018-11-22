package com.lhiot.healthygood.mapper.user;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
* Description:鲜果师微信用户Mapper类
* @author yijun
* @date 2018/07/26
*/
@Mapper
public interface FruitDoctorUserMapper {

    /**
    * Description:新增鲜果师微信用户
    *
    * @param fruitDoctorUser
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */
    int create(FruitDoctorUser fruitDoctorUser);

    /**
    * Description:根据id修改鲜果师微信用户
    *
    * @param fruitDoctorUser
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */
    int updateById(FruitDoctorUser fruitDoctorUser);

    /**
    * Description:根据ids删除鲜果师微信用户
    *
    * @param ids
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */
    int deleteByIds(List<String> ids);

    /**
    * Description:根据id查找鲜果师微信用户
    *
    * @param id
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */
    FruitDoctorUser selectById(Long id);


    /**
     * Description:根据openId查找鲜果师微信用户
     *
     * @param openId
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    FruitDoctorUser selectByOpenId(String openId);


    /**
    * Description:查询鲜果师微信用户列表
    *
    * @param fruitDoctorUser
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */
     List<FruitDoctorUser> pageFruitDoctorUsers(FruitDoctorUser fruitDoctorUser);


    /**
    * Description: 查询鲜果师微信用户总记录数
    *
    * @param fruitDoctorUser
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */
    Integer pageFruitDoctorUserCounts(FruitDoctorUser fruitDoctorUser);

	//根据鲜果师id查询用户信息
	List<FruitDoctorUser> findUserByDoctorId(Long doctorId);

	FruitDoctorUser findByDoctorIdAndUserId(Map<String, Object> map);
	
	FruitDoctorUser findByPhone(String phone);
	
	FruitDoctorUser findFruitDoctorUser(Long userId);
	
}
