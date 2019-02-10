package com.lhiot.healthygood.mapper.doctor;


import com.lhiot.healthygood.domain.doctor.Achievement;
import com.lhiot.healthygood.domain.doctor.DoctorAchievementLog;
import com.lhiot.healthygood.domain.doctor.IncomeStat;
import com.lhiot.healthygood.domain.doctor.TeamAchievement;
import com.lhiot.healthygood.type.SourceType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Description:鲜果师业绩记录Mapper类
 *
 * @author yijun
 * @date 2018/07/26
 */
@Mapper
@Repository
public interface DoctorAchievementLogMapper {

    /**
     * Description:新增鲜果师业绩记录
     *
     * @param doctorAchievementLog
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    int create(DoctorAchievementLog doctorAchievementLog);

    /**
     * Description:根据id修改鲜果师业绩记录
     *
     * @param doctorAchievementLog
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    int updateById(DoctorAchievementLog doctorAchievementLog);

    /**
     * Description:根据ids删除鲜果师业绩记录
     *
     * @param ids
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    int deleteByIds(List<String> ids);

    /**
     * Description:根据id查找鲜果师业绩记录
     *
     * @param id
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    DoctorAchievementLog selectById(Long id);

    /**
     * Description:查询鲜果师业绩记录列表
     *
     * @param doctorAchievementLog
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    List<DoctorAchievementLog> pageDoctorAchievementLogs(DoctorAchievementLog doctorAchievementLog);

    List<DoctorAchievementLog> selectOrderCodeByDoctorId(Long doctorId);


    /**
     * Description: 查询鲜果师业绩记录总记录数
     *
     * @param doctorAchievementLog
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    int pageDoctorAchievementLogCounts(DoctorAchievementLog doctorAchievementLog);


    /**
     * Description: 我的收入
     *
     * @param id
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    IncomeStat myIncome(Long id);

    /**
     * Description: 我的团队的个人业绩
     *
     * @param id 鲜果师id
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    TeamAchievement teamAchievement(Long id);

    //统计鲜果师的业绩
    Achievement achievement(Map<String, Object> map);

    Long achievementTodayOrderCount(Map<String, Object> map);

    //根据订单id查询记录
    DoctorAchievementLog selectByOrderId(Long orderId);

    DoctorAchievementLog selectByOrderIdAndType(@Param("orderId") Long orderId, @Param("sourceType") SourceType sourceType);

    Integer selectFruitDoctorCommission(Long id);

    Integer superiorBonusOfMonth(Map<String, Object> map);
}
