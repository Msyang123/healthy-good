package com.lhiot.healthygood.feign.model;

import com.lhiot.healthygood.feign.type.ApplicationType;
import com.lhiot.healthygood.feign.type.PayStep;
import com.lhiot.healthygood.feign.type.SourceType;
import com.lhiot.healthygood.feign.type.TradeType;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Data
@ToString
public class Record implements Serializable {

    private Long id;

    private Long userId;

    private String userPhone;

    private String userRealName;

    private String userIdCard;

    private String orderCode;

    private ApplicationType applicationType;

    private TradeType tradeType;

    private SourceType sourceType;

    private String configName;

    private Long fee;

    private PayStep payStep;

    private String tradeId;

    private Date signAt;

    private Date payAt;

    private String memo;

    private String bankType;

    private String openId;

    private String clientIp;

    private String attach;
}
