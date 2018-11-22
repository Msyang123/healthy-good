package com.lhiot.healthygood.domain.template;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Map;

@Data
@ApiModel
public class TemplateParam {
    private String touser;
    private String template_id;
    private String url;
    private Map<String,Object> data;
}
