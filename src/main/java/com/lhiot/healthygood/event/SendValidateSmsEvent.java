package com.lhiot.healthygood.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendValidateSmsEvent implements Serializable {
    
    private String code;
    
    private String phone;
}
