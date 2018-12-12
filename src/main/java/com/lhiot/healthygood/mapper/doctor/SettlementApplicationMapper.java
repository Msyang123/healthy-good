package com.lhiot.healthygood.mapper.doctor;

import com.lhiot.healthygood.domain.doctor.SettlementApplication;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
* Description:结算申请Mapper类
* @author yijun
* @date 2018/07/26
*/
@Mapper
@Repository
public interface SettlementApplicationMapper {

    /**
    * Description:新增结算申请
    *
    * @param settlementApplication
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */
    int create(SettlementApplication settlementApplication);

    /**
    * Description:根据id修改结算申请
    *
    * @param settlementApplication
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */
    int updateById(SettlementApplication settlementApplication);

    /**
    * Description:根据ids删除结算申请
    *
    * @param ids
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */
    int deleteByIds(List<String> ids);

    /**
    * Description:根据id查找结算申请
    *
    * @param id
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */
    SettlementApplication selectById(Long id);

    /**
    * Description:查询结算申请列表
    *
    * @param settlementApplication
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */
     List<SettlementApplication> pageSettlementApplications(SettlementApplication settlementApplication);


    /**
    * Description: 查询结算申请总记录数
    *
    * @param settlementApplication
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */
    int pageSettlementApplicationCounts(SettlementApplication settlementApplication);

    /**
     * Description: 修改结算申请失效状态
     *
     * @param list
     * @return
     * @author Limiaojun
     * @date 2018/08/22 09:52:13
     */
    long updateExpiredStatus(List<Long> list);
}
