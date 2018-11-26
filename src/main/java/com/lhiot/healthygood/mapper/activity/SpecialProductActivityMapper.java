package com.lhiot.healthygood.mapper.activity;

import com.lhiot.healthygood.domain.activity.SpecialProductActivity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* Description:新品尝鲜活动Mapper类
* @author yangjiawen
* @date 2018/11/24
*/
@Mapper
public interface SpecialProductActivityMapper {

    /**
    * Description:新增新品尝鲜活动
    *
    * @param specialProductActivity
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */
    int create(SpecialProductActivity specialProductActivity);

    /**
    * Description:根据id修改新品尝鲜活动
    *
    * @param specialProductActivity
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */
    int updateById(SpecialProductActivity specialProductActivity);

    /**
    * Description:根据ids删除新品尝鲜活动
    *
    * @param ids
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */
    int deleteByIds(List<String> ids);

    /**
    * Description:根据id查找新品尝鲜活动
    *
    * @param id
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */
    SpecialProductActivity selectById(Long id);

    /**
    * Description:查询新品尝鲜活动列表
    *
    * @param specialProductActivity
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */
     List<SpecialProductActivity> pageSpecialProductActivitys(SpecialProductActivity specialProductActivity);


    /**
    * Description: 查询新品尝鲜活动总记录数
    *
    * @param specialProductActivity
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */
    long pageSpecialProductActivityCounts(SpecialProductActivity specialProductActivity);
}
