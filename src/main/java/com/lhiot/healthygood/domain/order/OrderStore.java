package com.lhiot.healthygood.domain.order;

import lombok.Data;

import java.time.Instant;
import java.util.Date;

/**
 * @author zhangshu created in 2018/9/18 15:29
 **/
@Data
public class OrderStore {
    private String hdOrderCode;
    private Long orderId;
    private Long storeId;
    private String storeCode;
    private String storeName;
    private String operationUser;
    private Date createAt = Date.from(Instant.now());
}
