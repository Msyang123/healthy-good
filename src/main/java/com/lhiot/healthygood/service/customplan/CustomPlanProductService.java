package com.lhiot.healthygood.service.customplan;


import com.lhiot.healthygood.domain.customplan.CustomPlanProduct;
import com.lhiot.healthygood.mapper.customplan.CustomPlanProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

/**
* Description:定制计划关联商品服务类
* @author hufan
* @date 2018/11/26
*/
@Service
@Transactional
public class CustomPlanProductService {

    private final CustomPlanProductMapper customPlanProductMapper;

    @Autowired
    public CustomPlanProductService(CustomPlanProductMapper customPlanProductMapper) {
        this.customPlanProductMapper = customPlanProductMapper;
    }

    /** 
    * Description:新增定制计划关联商品
    *  
    * @param customPlanProduct
    * @return
    * @author hufan
    * @date 2018/11/26 18:56:50
    */  
    public int create(CustomPlanProduct customPlanProduct){
        return this.customPlanProductMapper.create(customPlanProduct);
    }

    /** 
    * Description:根据id修改定制计划关联商品
    *  
    * @param customPlanProduct
    * @return
    * @author hufan
    * @date 2018/11/26 18:56:50
    */ 
    public int updateById(CustomPlanProduct customPlanProduct){
        return this.customPlanProductMapper.updateById(customPlanProduct);
    }

    /** 
    * Description:根据ids删除定制计划关联商品
    *  
    * @param ids
    * @return
    * @author hufan
    * @date 2018/11/26 18:56:50
    */ 
    public int deleteByIds(String ids){
        return this.customPlanProductMapper.deleteByIds(Arrays.asList(ids.split(",")));
    }
    
    /** 
    * Description:根据id查找定制计划关联商品
    *  
    * @param id
    * @return
    * @author hufan
    * @date 2018/11/26 18:56:50
    */ 
    public CustomPlanProduct selectById(Long id){
        return this.customPlanProductMapper.selectById(id);
    }

    /** 
    * Description: 查询定制计划关联商品总记录数
    *  
    * @param customPlanProduct
    * @return
    * @author hufan
    * @date 2018/11/26 18:56:50
    */  
    public long count(CustomPlanProduct customPlanProduct){
        return this.customPlanProductMapper.pageCustomPlanProductCounts(customPlanProduct);
    }
    
    /** 
    * Description: 查询定制计划关联商品分页列表
    *  
    * @param customPlanProduct
    * @return
    * @author hufan
    * @date 2018/11/26 18:56:50
    */
    // FIXME
   /* public PagerResultObject<CustomPlanProduct> pageList(CustomPlanProduct customPlanProduct) {
       long total = 0;
       if (customPlanProduct.getRows() != null && customPlanProduct.getRows() > 0) {
           total = this.count(customPlanProduct);
       }
       return PagerResultObject.of(customPlanProduct, total,
              this.customPlanProductMapper.pageCustomPlanProducts(customPlanProduct));
    }*/
}

