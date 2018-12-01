package com.lhiot.healthygood.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 短信验证码发送事件对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendCaptchaSmsEvent implements Serializable {

    private String phone;

}
