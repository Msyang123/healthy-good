package com.lhiot.healthygood.service.order;

import com.lhiot.healthygood.feign.model.CreateOrderParam;
import com.lhiot.healthygood.feign.model.OrderProduct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
/**
 * Description:服务类
 * @author yangjiawen
 * @date 2018/07/26
 */
@Service
@Transactional
public class OrderService {
    public boolean validationParam(CreateOrderParam orderParam) {
        boolean flag = false;
        //商品为空
        List<OrderProduct> orderProducts = orderParam.getOrderProducts();
        if(Objects.isNull(orderProducts) || orderProducts.isEmpty()){
            return flag;
        }
        //应付金额为空或者小于零
        int amountPayable = orderParam.getAmountPayable();
        if(Objects.isNull(amountPayable) || amountPayable <= 0){
            return flag;
        }
        Integer couponAmount = Objects.isNull(orderParam.getCouponAmount())?0:orderParam.getCouponAmount();
        if(couponAmount >= amountPayable){
            return flag;
        }
        flag = true;
        return flag;
    }
}
