package com.lhiot.healthygood.domain.template;

import com.lhiot.healthygood.util.DataItem;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Map;

@Data
@ApiModel
public class TemplateParam {
    private String touser;
    private String template_id;
    private String url;
    private DataItem data;

    @Override
    public String toString() {
		/*return "{" +
				"'templateId': '" + templateId + "'" +
				", 'templateName': '" + templateName + "'" +
				", 'url':'" + url + "'" +
				", 'touser':" + touser + "'" +
				", 'data':'" + data + "'" +
				"}";*/
        return "{\"template_id\": \""+template_id+"\",  \"url\":\""+url+"\", \"touser\":\""+touser+"\", \"data\":"+data+"}";
    }
}
