package com.lhiot.healthygood.mapper.activity;

import com.lhiot.healthygood.domain.activity.ActivityProduct;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* Description:活动商品Mapper类
* @author yangjiawen
* @date 2018/11/24
*/
@Mapper
public interface ActivityProductMapper {

    /**
    * Description:新增活动商品
    *
    * @param activityProduct
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */
    int create(ActivityProduct activityProduct);

    /**
    * Description:根据id修改活动商品
    *
    * @param activityProduct
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */
    int updateById(ActivityProduct activityProduct);

    /**
    * Description:根据ids删除活动商品
    *
    * @param ids
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */
    int deleteByIds(List<String> ids);

    /**
    * Description:根据id查找活动商品
    *
    * @param id
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */
    ActivityProduct selectById(Long id);

    /**
    * Description:查询活动商品列表
    *
    * @param activityProduct
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */
     List<ActivityProduct> pageActivityProducts(ActivityProduct activityProduct);


    /**
    * Description: 查询活动商品总记录数
    *
    * @param activityProduct
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */
    long pageActivityProductCounts(ActivityProduct activityProduct);
}
