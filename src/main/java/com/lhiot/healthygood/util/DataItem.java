package com.lhiot.healthygood.util;

import com.lhiot.healthygood.type.TemplateMessageEnum;
import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel
public class DataItem {
    private String keyword1Value;
    private String keyword2Value;
    private String keyword3Value;
    private String remark;

    @Override
    public String toString() {
        return "Data:{" +
                "keyword1Value='" + keyword1Value + '\'' +
                ", keyword2Value='" + keyword2Value + '\'' +
                ", keyword3Value='" + keyword3Value + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}
