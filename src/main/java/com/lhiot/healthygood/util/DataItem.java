package com.lhiot.healthygood.util;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel
public class DataItem {
    private DataObject first;
    private DataObject keyword1;
    private DataObject keyword2;
    private DataObject keyword3;
    private DataObject keyword4;
    private DataObject remark;

    @Override
    public String toString() {
        return "{ \"first\":"+first+",\"keyword1\":"+keyword1+",\"keyword2\": "+keyword2+",\"keyword3\": "+keyword3+",\"keyword4\":"+keyword4+",\"remark\":"+remark+"}";
    }
}
