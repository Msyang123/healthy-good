package com.lhiot.healthygood.wechat;

import lombok.Data;

@Data
public class JsapiTicket {
	 /** 
     * 有效时长 
     */  
    private int expiresIn;  
    /** 
     * js调用票据 
     */  
    private String ticket;
}
