package com.lhiot.healthygood.feign.model;

import java.io.Serializable;

/**
 * @author Leon (234239150@qq.com) created in 11:42 18.9.15
 */
public interface PayloadConverter extends Serializable {

    Payload toPayload();

}
