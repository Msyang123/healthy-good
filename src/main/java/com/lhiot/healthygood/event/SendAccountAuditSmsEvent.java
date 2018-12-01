package com.lhiot.healthygood.event;

import com.lhiot.healthygood.feign.type.AccountAuditStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 账户审核结果发送短信事件对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendAccountAuditSmsEvent implements Serializable {

    private String account;

    private String phone;

    private AccountAuditStatus accountAuditStatus;

}
