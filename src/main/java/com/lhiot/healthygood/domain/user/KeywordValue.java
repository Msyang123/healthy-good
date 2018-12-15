package com.lhiot.healthygood.domain.user;

import com.lhiot.healthygood.type.TemplateMessageEnum;
import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel
public class KeywordValue {
    private TemplateMessageEnum templateType;
    private Long userId;
    private String keyword1Value;
    private String keyword2Value;
    private String keyword3Value;
    private String keyword4Value;
    private String keyword5Value;
    private String orderCode;

    //是否需要给鲜果师发送模板消息
    private boolean sendToDoctor;
}
