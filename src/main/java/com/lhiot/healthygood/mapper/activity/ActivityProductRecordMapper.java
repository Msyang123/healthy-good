package com.lhiot.healthygood.mapper.activity;

import com.lhiot.healthygood.domain.activity.ActivityProductRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
* Description:活动商品购买记录Mapper类
* @author yangjiawen
* @date 2018/11/24
*/
@Mapper
public interface ActivityProductRecordMapper {

    /**
    * Description:新增活动商品购买记录
    *
    * @param activityProductRecord
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */
    int create(ActivityProductRecord activityProductRecord);

    /**
    * Description:根据id修改活动商品购买记录
    *
    * @param activityProductRecord
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */
    int updateById(ActivityProductRecord activityProductRecord);

    /**
    * Description:根据ids删除活动商品购买记录
    *
    * @param ids
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */
    int deleteByIds(List<String> ids);

    /**
    * Description:根据id查找活动商品购买记录
    *
    * @param id
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */
    ActivityProductRecord selectById(Long id);

    Integer selectRecordCount(Long userId);

    /**
    * Description:查询活动商品购买记录列表
    *
    * @param activityProductRecord
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */
     List<ActivityProductRecord> pageActivityProductRecords(ActivityProductRecord activityProductRecord);


    /**
    * Description: 查询活动商品购买记录总记录数
    *
    * @param activityProductRecord
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */
    long pageActivityProductRecordCounts(ActivityProductRecord activityProductRecord);
}
