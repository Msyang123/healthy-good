package com.lhiot.healthygood.feign.model;

import com.lhiot.healthygood.type.FreeSignName;
import lombok.Data;
import lombok.ToString;

import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Leon (234239150@qq.com) created in 11:52 18.9.7
 */
@Data
@ToString
public class Payload {

    private String phoneNumber;

    private FreeSignName freeSignName;

    /**
     * 模版中的变量集合
     */
    private Map<String, String> variables;

    /**
     * 扩展参数
     */
    private String extend;

    public Payload(String phoneNumber, FreeSignName freeSignName, String extend) {
        this.phoneNumber = phoneNumber;
        this.freeSignName = freeSignName;
        this.extend = extend;
    }

    public void withVars(Supplier<Map<String, String>> supplier) {
        this.variables = supplier.get();
    }
}
