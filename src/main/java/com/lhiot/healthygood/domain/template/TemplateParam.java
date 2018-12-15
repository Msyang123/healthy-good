package com.lhiot.healthygood.domain.template;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Map;

@Data
@ApiModel
public class TemplateParam {
    private String touser;
    private String templateId;
    private String url;
    private String data;
}
