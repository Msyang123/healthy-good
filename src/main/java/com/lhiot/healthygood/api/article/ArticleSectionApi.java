package com.lhiot.healthygood.api.article;

import com.leon.microx.util.StringUtils;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.session.Sessions;
import com.lhiot.healthygood.feign.BaseDataServiceFeign;
import com.lhiot.healthygood.feign.model.ArticleSection;
import com.lhiot.healthygood.feign.model.ArticleSectionParam;
import com.lhiot.healthygood.feign.model.UiPosition;
import com.lhiot.healthygood.feign.model.UiPositionParam;
import com.lhiot.healthygood.feign.type.ApplicationType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Api(description = "文章板块类接口(no session)")
@Slf4j
@RestController
public class ArticleSectionApi {
    private final BaseDataServiceFeign baseDataServiceFeign;

    @Autowired
    public ArticleSectionApi(BaseDataServiceFeign baseDataServiceFeign) {
        this.baseDataServiceFeign = baseDataServiceFeign;
    }

    @Sessions.Uncheck
    @GetMapping("/article-section/recommend")
    @ApiOperation(value = "发现文章推荐列表", response = ArticleSection.class)
    public ResponseEntity recommendArticle() {
        UiPositionParam uiPositionParam = new UiPositionParam();
        uiPositionParam.setApplicationType(ApplicationType.HEALTH_GOOD);
        uiPositionParam.setCodes("SEARCH_ARTICLES");
        ResponseEntity uiPositionEntity = baseDataServiceFeign.searchUiPosition(uiPositionParam);
        if (Objects.isNull(uiPositionEntity) || uiPositionEntity.getStatusCode().isError()) {
            return uiPositionEntity;
        }
        List<String> positionIds = new ArrayList<>();
        ((Pages<UiPosition>) uiPositionEntity.getBody()).getArray().forEach(uiPosition -> {
            positionIds.add(uiPosition.getId().toString());
        });
        ArticleSectionParam articleSectionParam = new ArticleSectionParam();
        articleSectionParam.setApplicationType(ApplicationType.HEALTH_GOOD);
        articleSectionParam.setPositionIds(StringUtils.collectionToDelimitedString(positionIds, ","));
        articleSectionParam.setIncludeArticles(true);
        ResponseEntity pagesResponseEntity = baseDataServiceFeign.searchArticleSection(articleSectionParam);
        return ResponseEntity.ok(((Pages<ArticleSection>) pagesResponseEntity.getBody()).getArray().get(0));
    }
}
