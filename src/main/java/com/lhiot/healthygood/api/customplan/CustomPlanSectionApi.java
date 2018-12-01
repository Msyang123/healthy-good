package com.lhiot.healthygood.api.customplan;

import com.leon.microx.util.Maps;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.leon.microx.web.session.Sessions;
import com.leon.microx.web.swagger.ApiParamType;
import com.lhiot.healthygood.domain.customplan.CustomPlanSection;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanSectionParam;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanSectionResultAdmin;
import com.lhiot.healthygood.service.customplan.CustomPlanSectionRelationService;
import com.lhiot.healthygood.service.customplan.CustomPlanSectionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Objects;

/**
 * @author hufan created in 2018/11/27 10:42
 **/
@Api(description = "定制板块接口")
@Slf4j
@RestController
public class CustomPlanSectionApi {
    private final CustomPlanSectionService customPlanSectionService;
    private final CustomPlanSectionRelationService customPlanSectionRelationService;

    @Autowired
    public CustomPlanSectionApi(CustomPlanSectionService customPlanSectionService, CustomPlanSectionRelationService customPlanSectionRelationService) {
        this.customPlanSectionService = customPlanSectionService;
        this.customPlanSectionRelationService = customPlanSectionRelationService;
    }

    @Sessions.Uncheck
    @ApiOperation("添加定制板块(后台)")
    @ApiImplicitParam(paramType = ApiParamType.BODY, name = "customPlanSectionResultAdmin", value = "定制计划板块", dataType = "CustomPlanSectionResultAdmin", required = true)
    @PostMapping("/custom-plan-sections")
    public ResponseEntity create(@RequestBody CustomPlanSectionResultAdmin customPlanSectionResultAdmin) {
        log.debug("添加定制板块\t param:{}", customPlanSectionResultAdmin);

        // 添加定制板块
        Tips tips = customPlanSectionService.addCustomPlanSection(customPlanSectionResultAdmin);
        if (tips.err()) {
            return ResponseEntity.badRequest().body(tips.getMessage());
        }
        Long customPlanSectionId = Long.valueOf(tips.getMessage());
        return customPlanSectionId > 0 ?
                ResponseEntity.created(URI.create("/custom-plan-sections/" + customPlanSectionId)).body(Maps.of("id", customPlanSectionId)) :
                ResponseEntity.badRequest().body("添加定制板块失败!");
    }

    @Sessions.Uncheck
    @ApiOperation("修改定制板块(后台)")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = ApiParamType.PATH, name = "id", value = "定制板块id", dataType = "Long", required = true),
            @ApiImplicitParam(paramType = ApiParamType.BODY, name = "customPlanSectionResultAdmin", value = "定制板块", dataType = "CustomPlanSectionResultAdmin", required = true)
    })
    @PutMapping("/custom-plan-sections/{id}")
    public ResponseEntity update(@PathVariable("id") Long id, @RequestBody CustomPlanSectionResultAdmin customPlanSectionResultAdmin) {
        log.debug("修改定制板块\t param:{}", customPlanSectionResultAdmin);

        customPlanSectionResultAdmin.setId(id);
        return customPlanSectionService.update(customPlanSectionResultAdmin) ? ResponseEntity.ok().build() : ResponseEntity.badRequest().body("修改信息失败!");
    }

    @Sessions.Uncheck
    @ApiOperation(value = "根据id查找单个定制板块(后台)", response = CustomPlanSectionResultAdmin.class)
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = ApiParamType.PATH, name = "id", value = "定制板块id", dataType = "Long", required = true),
            @ApiImplicitParam(paramType = ApiParamType.QUERY, name = "flag", value = "是否查询关联定制计划信息", dataType = "Boolean")
    })
    @GetMapping("/custom-plan-sections/{id}")
    public ResponseEntity findById(@PathVariable("id") Long id, @RequestParam(value = "flag", required = false)  boolean flag) {
        log.debug("根据id查找单个定制板块\t param:{}", id, flag);

        CustomPlanSectionResultAdmin customPlanSectionResult = customPlanSectionService.findById(id, flag);
        return ResponseEntity.ok().body(customPlanSectionResult);
    }

    @Sessions.Uncheck
    @ApiOperation("根据ids删除定制板块(后台)")
    @ApiImplicitParam(paramType = ApiParamType.PATH, name = "ids", value = "多个定制板块id以英文逗号分隔", dataType = "String", required = true)
    @DeleteMapping("/custom-plan-sections/{ids}")
    public ResponseEntity batchDelete(@PathVariable("ids") String ids) {
        log.debug("批量删除定制板块\t param:{}", ids);

        List<String> sectionIds = customPlanSectionRelationService.findBySectionIds(ids);
        if (Objects.nonNull(sectionIds)) {
            return ResponseEntity.badRequest().body("以下定制板块id不可删除：" +  sectionIds.toString());
        }
        return customPlanSectionService.batchDeleteByIds(ids) ? ResponseEntity.noContent().build() : ResponseEntity.badRequest().body("删除信息失败!");
    }

    @Sessions.Uncheck
    @ApiOperation(value = "根据条件分页查询定制板块信息列表(后台)", response = CustomPlanSection.class, responseContainer = "Set")
    @ApiImplicitParam(paramType = ApiParamType.BODY, name = "param", value = "查询条件", dataType = "CustomPlanSectionParam")
    @PostMapping("/custom-plan-sections/pages")
    public ResponseEntity search(@RequestBody CustomPlanSectionParam param) {
        log.debug("根据条件分页查询定制板块信息列表\t param:{}", param);

        Pages<CustomPlanSection> pages = customPlanSectionService.findList(param);
        return ResponseEntity.ok(pages);
    }
}