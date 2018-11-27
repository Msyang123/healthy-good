package com.lhiot.healthygood.service.activity;

import com.lhiot.healthygood.common.PagerResultObject;
import com.lhiot.healthygood.domain.activity.ActivityProductRecord;
import com.lhiot.healthygood.mapper.activity.ActivityProductRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;


/**
* Description:活动商品购买记录服务类
* @author yangjiawen
* @date 2018/11/24
*/
@Service
@Transactional
public class ActivityProductRecordService {

    private final ActivityProductRecordMapper activityProductRecordMapper;

    @Autowired
    public ActivityProductRecordService(ActivityProductRecordMapper activityProductRecordMapper) {
        this.activityProductRecordMapper = activityProductRecordMapper;
    }

    /** 
    * Description:新增活动商品购买记录
    *  
    * @param activityProductRecord
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */  
    public Integer create(ActivityProductRecord activityProductRecord){
        return this.activityProductRecordMapper.create(activityProductRecord);
    }

    /** 
    * Description:根据id修改活动商品购买记录
    *  
    * @param activityProductRecord
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */ 
    public Integer updateById(ActivityProductRecord activityProductRecord){
        return this.activityProductRecordMapper.updateById(activityProductRecord);
    }

    /** 
    * Description:根据ids删除活动商品购买记录
    *  
    * @param ids
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */ 
    public Integer deleteByIds(String ids){
        return this.activityProductRecordMapper.deleteByIds(Arrays.asList(ids.split(",")));
    }
    
    /** 
    * Description:根据id查找活动商品购买记录
    *  
    * @param id
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */ 
    public ActivityProductRecord selectById(Long id){
        return this.activityProductRecordMapper.selectById(id);
    }

    public Integer selectRecordCount(Long userId){
        return activityProductRecordMapper.selectRecordCount(userId);
    }

    /** 
    * Description: 查询活动商品购买记录总记录数
    *  
    * @param activityProductRecord
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */  
    public Long count(ActivityProductRecord activityProductRecord){
        return this.activityProductRecordMapper.pageActivityProductRecordCounts(activityProductRecord);
    }
    
    /** 
    * Description: 查询活动商品购买记录分页列表
    *  
    * @param activityProductRecord
    * @return
    * @author yangjiawen
    * @date 2018/11/24 16:09:12
    */  
    public PagerResultObject<ActivityProductRecord> pageList(ActivityProductRecord activityProductRecord) {
       long total = 0;
       if (activityProductRecord.getRows() != null && activityProductRecord.getRows() > 0) {
           total = this.count(activityProductRecord);
       }
       return PagerResultObject.of(activityProductRecord, total,
              this.activityProductRecordMapper.pageActivityProductRecords(activityProductRecord));
    }
}

