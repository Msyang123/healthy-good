package com.lhiot.healthygood.service.doctor;

import com.leon.microx.web.result.Pages;
import com.lhiot.healthygood.domain.doctor.SettlementApplication;
import com.lhiot.healthygood.mapper.doctor.SettlementApplicationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

/**
* Description:结算申请服务类
* @author yijun
* @date 2018/07/26
*/
@Service
@Transactional
public class SettlementApplicationService {

    private final SettlementApplicationMapper settlementApplicationMapper;

    @Autowired
    public SettlementApplicationService(SettlementApplicationMapper settlementApplicationMapper) {
        this.settlementApplicationMapper = settlementApplicationMapper;
    }

    /** 
    * Description:新增结算申请
    *  
    * @param settlementApplication
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */  
    public int create(SettlementApplication settlementApplication){
        return this.settlementApplicationMapper.create(settlementApplication);
    }

    /** 
    * Description:根据id修改结算申请
    *  
    * @param settlementApplication
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */ 
    public int updateById(SettlementApplication settlementApplication){
        return this.settlementApplicationMapper.updateById(settlementApplication);
    }

    /** 
    * Description:根据ids删除结算申请
    *  
    * @param ids
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */ 
    public int deleteByIds(String ids){
        return this.settlementApplicationMapper.deleteByIds(Arrays.asList(ids.split(",")));
    }
    
    /** 
    * Description:根据id查找结算申请
    *  
    * @param id
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */ 
    public SettlementApplication selectById(Long id){
        return this.settlementApplicationMapper.selectById(id);
    }

    /** 
    * Description: 查询结算申请总记录数
    *  
    * @param settlementApplication
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */  
    public int count(SettlementApplication settlementApplication){
        return this.settlementApplicationMapper.pageSettlementApplicationCounts(settlementApplication);
    }
    
    /** 
    * Description: 查询结算申请分页列表
    *  
    * @param settlementApplication
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */  
    public Pages<SettlementApplication> pageList(SettlementApplication settlementApplication) {
       int total = 0;
       if (settlementApplication.getRows() != null && settlementApplication.getRows() > 0) {
           total = this.count(settlementApplication);
       }
       return Pages.of(total,this.settlementApplicationMapper.pageSettlementApplications(settlementApplication));
    }
}

