package com.lhiot.healthygood.mapper.user;

import com.lhiot.healthygood.domain.user.FruitDoctor;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Description:鲜果师成员Mapper类
 *
 * @author yijun
 * @date 2018/07/26
 */
@Repository
@Mapper
public interface FruitDoctorMapper {

    /**
     * Description:新增鲜果师成员
     *
     * @param fruitDoctor
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    int create(FruitDoctor fruitDoctor);

    /**
     * Description:根据id修改鲜果师成员
     *
     * @param fruitDoctor
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    int updateById(FruitDoctor fruitDoctor);

    /**
     * 鲜果师红利增减和可结算余额增加
     *
     * @param map
     * @return
     */
    int updateBouns(Map<String, Object> map);

    /**
     * Description:根据ids删除鲜果师成员
     *
     * @param ids
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    int deleteByIds(List<String> ids);

    /**
     * Description:根据id查找鲜果师成员
     *
     * @param id
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    FruitDoctor selectById(Long id);


    /**
     * Description:根据用户编号查找鲜果师成员
     *
     * @param userId
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    FruitDoctor selectByUserId(Long userId);

    /**
     * Description:查询鲜果师成员列表
     *
     * @param fruitDoctor
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    List<FruitDoctor> pageFruitDoctors(FruitDoctor fruitDoctor);


    /**
     * Description: 查询鲜果师成员总记录数
     *
     * @param fruitDoctor
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    int pageFruitDoctorCounts(FruitDoctor fruitDoctor);

    /**
     * Description:查询鲜果师团队列表
     *
     * @param fruitDoctor
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    List<FruitDoctor> subordinate(FruitDoctor fruitDoctor);

    FruitDoctor selectByInviteCode(String inviteCode);


    /**
     * Description:根据用户id查询上级鲜果师信息
     *
     * @param userId
     * @return
     * @author hufan
     * @date 2018/08/21 12:08:13
     */
    FruitDoctor findSuperiorFruitDoctorByUserId(Long userId);

    /**
     * Description:根据用户id查询鲜果师信息
     *
     * @param userId
     * @return
     * @author hufan
     * @date 2018/08/21 12:08:13
     */
    FruitDoctor findFruitDoctorByUserId(Long userId);
}
