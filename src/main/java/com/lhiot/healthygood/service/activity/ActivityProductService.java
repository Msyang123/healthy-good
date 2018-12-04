package com.lhiot.healthygood.service.activity;

import com.leon.microx.web.result.Pages;
import com.lhiot.healthygood.domain.activity.ActivityProduct;
import com.lhiot.healthygood.mapper.activity.ActivityProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
* Description:活动商品服务类
* @author yangjiawen
* @date 2018/11/24
*/
@Service
@Transactional
public class ActivityProductService {

    private final ActivityProductMapper activityProductMapper;

    @Autowired
    public ActivityProductService(ActivityProductMapper activityProductMapper) {
        this.activityProductMapper = activityProductMapper;
    }

    /** 
    * Description:新增活动商品
    *  
    * @param activityProduct
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */  
    public Integer create(ActivityProduct activityProduct){
        return this.activityProductMapper.create(activityProduct);
    }

    /** 
    * Description:根据id修改活动商品
    *  
    * @param activityProduct
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */ 
    public Integer updateById(ActivityProduct activityProduct){
        return this.activityProductMapper.updateById(activityProduct);
    }

    /** 
    * Description:根据ids删除活动商品
    *  
    * @param ids
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */ 
    public Integer deleteByIds(String ids){
        return this.activityProductMapper.deleteByIds(Arrays.asList(ids.split(",")));
    }
    
    /** 
    * Description:根据id查找活动商品
    *  
    * @param id
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */ 
    public ActivityProduct selectById(Long id){
        return this.activityProductMapper.selectById(id);
    }

    public ActivityProduct selectActivityProduct(ActivityProduct activityProduct){
        return activityProductMapper.selectActivityProduct(activityProduct);
    }

    /** 
    * Description: 查询活动商品总记录数
    *  
    * @param activityProduct
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */  
    public int count(ActivityProduct activityProduct){
        return this.activityProductMapper.pageActivityProductCounts(activityProduct);
    }
    
    /** 
    * Description: 查询活动商品分页列表
    *  
    * @param activityProduct
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */  
    public Pages<ActivityProduct> pageList(ActivityProduct activityProduct) {
       int total = 0;
       if (activityProduct.getRows() != null && activityProduct.getRows() > 0) {
           total = this.count(activityProduct);
       }
       return Pages.of(total,
              this.activityProductMapper.pageActivityProducts(activityProduct));
    }

    public List<ActivityProduct> activityProductList(ActivityProduct activityProduct){
        return this.activityProductMapper.pageActivityProducts(activityProduct);
    }
}

