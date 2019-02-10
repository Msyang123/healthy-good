package com.lhiot.healthygood.feign;

import com.lhiot.healthygood.feign.model.FaqCategory;
import com.lhiot.healthygood.feign.model.FaqParam;
import com.lhiot.healthygood.feign.model.Feedback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Component
@FeignClient(value = "CONTENT-CENTER-SERVICE-V1-0")
public interface ContentServiceFeign {
    /**
     * faq查询
     * @param param
     * @return
     */
    @RequestMapping(value="/faqs/pages",method = RequestMethod.POST)
    ResponseEntity<List<FaqCategory>> faqList(@RequestBody FaqParam param);

    /**
     * 用户反馈创建
     * @param feedback
     * @return
     */
    @RequestMapping(value="/feedback",method = RequestMethod.POST)
    ResponseEntity<?> create(@RequestBody Feedback feedback);
}
