package com.lhiot.healthygood.feign.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.Date;

/**
 * @author zhangfeng created in 2018/9/18 15:29
 **/
@Data
public class OrderStore {
    private String hdOrderCode;
    private Long orderId;
    private Long storeId;
    @NotBlank(message = "门店编码不能为空")
    private String storeCode;
    private String storeName;
    private String operationUser;
}
