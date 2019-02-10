package com.lhiot.healthygood.mapper.activity;

import com.lhiot.healthygood.domain.activity.ActivityProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Description:活动商品Mapper类
 *
 * @author yangjiawen
 * @date 2018/11/24
 */
@Mapper
@Repository
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

    ActivityProduct selectActivityProduct(ActivityProduct activityProduct);

    List<ActivityProduct> selectActivityProducts(ActivityProduct activityProduct);

    /**
     * Description:根据id查找活动商品
     *
     * @param ids
     * @return
     * @author hufan
     * @date 2018/12/03 16:09:12
     */
    List<ActivityProduct> selectByIds(@Param("ids") String ids);

    /**
     * Description:根据id查找活动商品
     *
     * @param specialProductActivityId
     * @param productShelfId
     * @return
     * @author hufan
     * @date 2018/12/03 18:42:15
     */
    ActivityProduct selectBySpecialIdAndShelfId(@Param("specialProductActivityId") Long specialProductActivityId, @Param("productShelfId") Long productShelfId);

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
    int pageActivityProductCounts(ActivityProduct activityProduct);
}
