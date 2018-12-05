package com.lhiot.healthygood.api.article;

import com.leon.microx.util.BeanUtils;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.leon.microx.web.session.Sessions;
import com.leon.microx.web.swagger.ApiParamType;
import com.lhiot.healthygood.domain.article.ArticleSearchParam;
import com.lhiot.healthygood.feign.BaseDataServiceFeign;
import com.lhiot.healthygood.feign.model.Article;
import com.lhiot.healthygood.feign.model.ArticleParam;
import com.lhiot.healthygood.feign.type.ArticleStatus;
import com.lhiot.healthygood.util.FeginResponseTools;
import io.swagger.annotations.Api;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Api(description = "文章类接口")
@Slf4j
@RestController
public class ArticleApi {
    private final BaseDataServiceFeign baseDataServiceFeign;

    public ArticleApi(BaseDataServiceFeign baseDataServiceFeign) {
        this.baseDataServiceFeign = baseDataServiceFeign;
    }

    @Sessions.Uncheck
    @PostMapping("/articles/search")
    @ApiImplicitParam(paramType = ApiParamType.BODY, name = "articleSearchParam", value = "文章搜索条件", dataType = "ArticleSearchParam")
    @ApiOperation(value = "查询/搜索文章")
    public ResponseEntity searchArticle(@RequestBody ArticleSearchParam articleSearchParam){
        ArticleParam articleParam = new ArticleParam();
        BeanUtils.copyProperties(articleSearchParam,articleParam);
        articleParam.setArticleStatus(ArticleStatus.PUBLISH);
        ResponseEntity<Pages<Article>> searchArticle = baseDataServiceFeign.searchArticle(articleParam);
        Tips<Pages<Article>> tips = FeginResponseTools.convertResponse(searchArticle);
        if (tips.err()) {
            return ResponseEntity.badRequest().body(tips.getData());
        }
        return  ResponseEntity.ok(tips.getData());
    }
}
