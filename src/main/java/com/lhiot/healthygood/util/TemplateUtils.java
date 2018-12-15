package com.lhiot.healthygood.util;

import com.google.common.collect.ImmutableMap;
import com.lhiot.dc.dictionary.module.Dictionary;
import com.lhiot.healthygood.domain.doctor.RegisterApplication;
import com.lhiot.healthygood.domain.template.TemplateData;
import com.lhiot.healthygood.domain.user.KeywordValue;
import com.lhiot.healthygood.feign.model.OrderDetailResult;
import com.lhiot.healthygood.feign.type.OrderStatus;
import com.lhiot.healthygood.type.AuditStatus;
import com.lhiot.healthygood.type.FirstAndRemarkData;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class TemplateUtils {
    /**
     * 发送模板消息的firstdata和remarkdata
     *
     * @param keywordValue
     * @return
     */
    public TemplateData sendTemplate(KeywordValue keywordValue) {
        return null;
    }

}
