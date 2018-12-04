package com.lhiot.healthygood.mapper.doctor;

import com.lhiot.healthygood.domain.doctor.CardUpdateLog;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
* Description:Mapper类
* @author yijun
* @date 2018/07/26
*/
@Mapper
@Repository
public interface CardUpdateLogMapper {

    /**
    * Description:新增
    *
    * @param cardUpdateLog
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */
    int create(CardUpdateLog cardUpdateLog);

    /**
    * Description:根据id修改
    *
    * @param cardUpdateLog
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */
    int updateById(CardUpdateLog cardUpdateLog);

    /**
    * Description:根据ids删除
    *
    * @param ids
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */
    int deleteByIds(List<String> ids);

    /**
    * Description:根据id查找
    *
    * @param id
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */
    CardUpdateLog selectById(Long id);

    CardUpdateLog selectByCard(CardUpdateLog cardUpdateLog);

    /**
    * Description:查询列表
    *
    * @param cardUpdateLog
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */
     List<CardUpdateLog> pageCardUpdateLogs(CardUpdateLog cardUpdateLog);


    /**
    * Description: 查询总记录数
    *
    * @param cardUpdateLog
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */
    int pageCardUpdateLogCounts(CardUpdateLog cardUpdateLog);
}
