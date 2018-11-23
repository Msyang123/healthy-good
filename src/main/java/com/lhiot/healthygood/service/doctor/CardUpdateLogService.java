package com.lhiot.healthygood.service.doctor;

import com.lhiot.healthygood.common.PagerResultObject;
import com.lhiot.healthygood.domain.doctor.CardUpdateLog;
import com.lhiot.healthygood.mapper.doctor.CardUpdateLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

/**
* Description:服务类
* @author yijun
* @date 2018/07/26
*/
@Service
@Transactional
public class CardUpdateLogService {

    private final CardUpdateLogMapper cardUpdateLogMapper;

    @Autowired
    public CardUpdateLogService(CardUpdateLogMapper cardUpdateLogMapper) {
        this.cardUpdateLogMapper = cardUpdateLogMapper;
    }

    /** 
    * Description:新增
    *  
    * @param cardUpdateLog
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */  
    public int create(CardUpdateLog cardUpdateLog){
        return this.cardUpdateLogMapper.create(cardUpdateLog);
    }

    /** 
    * Description:根据id修改
    *  
    * @param cardUpdateLog
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */ 
    public int updateById(CardUpdateLog cardUpdateLog){
        return this.cardUpdateLogMapper.updateById(cardUpdateLog);
    }

    /** 
    * Description:根据ids删除
    *  
    * @param ids
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */ 
    public int deleteByIds(String ids){
        return this.cardUpdateLogMapper.deleteByIds(Arrays.asList(ids.split(",")));
    }
    
    /** 
    * Description:根据id查找
    *  
    * @param id
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */ 
    public CardUpdateLog selectById(Long id){
        return this.cardUpdateLogMapper.selectById(id);
    }

    public CardUpdateLog selectByCard(CardUpdateLog cardUpdateLog){
        return this.cardUpdateLogMapper.selectByCard(cardUpdateLog);
    }

    /**
    * Description: 查询总记录数
    *  
    * @param cardUpdateLog
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */  
    public long count(CardUpdateLog cardUpdateLog){
        return this.cardUpdateLogMapper.pageCardUpdateLogCounts(cardUpdateLog);
    }
    
    /** 
    * Description: 查询分页列表
    *  
    * @param cardUpdateLog
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */  
    public PagerResultObject<CardUpdateLog> pageList(CardUpdateLog cardUpdateLog) {
       long total = 0;
       if (cardUpdateLog.getRows() != null && cardUpdateLog.getRows() > 0) {
           total = this.count(cardUpdateLog);
       }
       return PagerResultObject.of(cardUpdateLog, total,
              this.cardUpdateLogMapper.pageCardUpdateLogs(cardUpdateLog));
    }
}

