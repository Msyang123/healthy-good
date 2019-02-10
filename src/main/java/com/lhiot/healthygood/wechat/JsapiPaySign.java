package com.lhiot.healthygood.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 微信jsapi调用返回结果
 */
@Data
public class JsapiPaySign {
	 /** 
     * app应用id
     */
    private String appid;
    /** 
     * 时间（秒）
     */  
    private String timestamp;

    @JsonProperty("nonce_str")
    private String nonceStr;

    private String signature;
}
