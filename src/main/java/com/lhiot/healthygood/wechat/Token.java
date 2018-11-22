package com.lhiot.healthygood.wechat;

import lombok.Data;

import java.io.Serializable;

@Data
public class Token implements Serializable{
    private static final long serialVersionUID = -4093601016338089860L;
    private String accessToken;
    private int expiresIn;
    private String refreshToken;
    
}
