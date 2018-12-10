package com.lhiot.healthygood.api.article;

import com.leon.microx.web.result.Tips;
import com.leon.microx.web.session.Sessions;
import com.leon.microx.web.swagger.ApiParamType;
import com.lhiot.healthygood.feign.ContentServiceFeign;
import com.lhiot.healthygood.feign.model.FaqCategory;
import com.lhiot.healthygood.feign.model.FaqParam;
import com.lhiot.healthygood.feign.model.Feedback;
import com.lhiot.healthygood.util.FeginResponseTools;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(description = "内容类接口(faq,用户反馈)")
@Slf4j
@RestController
public class ContentApi {

    private final ContentServiceFeign contentServiceFeign;

    public ContentApi(ContentServiceFeign contentServiceFeign) {
        this.contentServiceFeign = contentServiceFeign;
    }

    @Sessions.Uncheck
    @GetMapping("/faqs")
    @ApiImplicitParam(paramType = ApiParamType.QUERY, name = "categoryEnName", value = "分类英文名称", dataType = "String")
    @ApiOperation(value = "常见问题",response = FaqCategory.class,responseContainer = "List")
    public ResponseEntity<Tips> faqs(@RequestParam String categoryEnName){
        FaqParam faqParam = new FaqParam();
        faqParam.setCategoryEnName(categoryEnName);
        faqParam.setApplicationType("FRUIT_DOCTOR");
        ResponseEntity<List<FaqCategory>> faqListRes = contentServiceFeign.faqList(faqParam);
        Tips tips = FeginResponseTools.convertResponse(faqListRes);
        return FeginResponseTools.returnTipsResponse(tips);
    }

    @PostMapping("/feedback")
    @ApiImplicitParam(paramType = ApiParamType.BODY, name = "feedback", value = "用户反馈", dataType = "Feedback")
    @ApiOperation(value = "用户反馈" )
    public ResponseEntity feedback(Sessions.User user, @RequestBody Feedback feedback){
        feedback.setUserId(Long.valueOf(user.getUser().get("userId").toString()));
        ResponseEntity feedbackRes = contentServiceFeign.create(feedback);
        Tips tips = FeginResponseTools.convertResponse(feedbackRes);
        if (tips.err()){
            return ResponseEntity.badRequest().body(tips);
        }
        return ResponseEntity.ok(tips.getData());
    }
}
