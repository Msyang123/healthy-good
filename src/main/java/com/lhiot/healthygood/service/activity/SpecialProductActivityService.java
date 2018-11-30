package com.lhiot.healthygood.service.activity;

import com.leon.microx.web.result.Pages;
import com.lhiot.healthygood.domain.activity.SpecialProductActivity;
import com.lhiot.healthygood.mapper.activity.SpecialProductActivityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

/**
* Description:新品尝鲜活动服务类
* @author yangjiawen
* @date 2018/11/24
*/
@Service
@Transactional
public class SpecialProductActivityService {

    private final SpecialProductActivityMapper specialProductActivityMapper;

    @Autowired
    public SpecialProductActivityService(SpecialProductActivityMapper specialProductActivityMapper) {
        this.specialProductActivityMapper = specialProductActivityMapper;
    }

    /** 
    * Description:新增新品尝鲜活动
    *  
    * @param specialProductActivity
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */  
    public Integer create(SpecialProductActivity specialProductActivity){
        return this.specialProductActivityMapper.create(specialProductActivity);
    }

    /** 
    * Description:根据id修改新品尝鲜活动
    *  
    * @param specialProductActivity
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */ 
    public Integer updateById(SpecialProductActivity specialProductActivity){
        return this.specialProductActivityMapper.updateById(specialProductActivity);
    }

    /** 
    * Description:根据ids删除新品尝鲜活动
    *  
    * @param ids
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */ 
    public Integer deleteByIds(String ids){
        return this.specialProductActivityMapper.deleteByIds(Arrays.asList(ids.split(",")));
    }
    
    /** 
    * Description:根据id查找新品尝鲜活动
    *  
    * @param id
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */ 
    public SpecialProductActivity selectById(Long id){
        return this.specialProductActivityMapper.selectById(id);
    }

    public SpecialProductActivity selectActivity(){
        return specialProductActivityMapper.selectActivity();
    }

    /** 
    * Description: 查询新品尝鲜活动总记录数
    *  
    * @param specialProductActivity
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */  
    public int count(SpecialProductActivity specialProductActivity){
        return this.specialProductActivityMapper.pageSpecialProductActivityCounts(specialProductActivity);
    }
    
    /** 
    * Description: 查询新品尝鲜活动分页列表
    *  
    * @param specialProductActivity
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */  
    public Pages<SpecialProductActivity> pageList(SpecialProductActivity specialProductActivity) {
       int total = 0;
       if (specialProductActivity.getRows() != null && specialProductActivity.getRows() > 0) {
           total = this.count(specialProductActivity);
       }
       return Pages.of(total,
              this.specialProductActivityMapper.pageSpecialProductActivitys(specialProductActivity));
    }
}

