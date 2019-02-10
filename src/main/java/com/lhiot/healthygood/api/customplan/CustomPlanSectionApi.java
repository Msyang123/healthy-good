package com.lhiot.healthygood.api.customplan;

import com.leon.microx.util.Maps;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.leon.microx.web.result.Tuple;
import com.leon.microx.web.session.Sessions;
import com.leon.microx.web.swagger.ApiParamType;
import com.lhiot.healthygood.domain.customplan.CustomPlanSection;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanSectionParam;
import com.lhiot.healthygood.domain.customplan.model.PlanSectionsParam;
import com.lhiot.healthygood.service.customplan.CustomPlanSectionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;

/**
 * @author hufan created in 2018/11/27 10:42
 **/
@Api(description = "定制板块接口(no session)")
@Slf4j
@RestController
public class CustomPlanSectionApi {
    private final CustomPlanSectionService customPlanSectionService;

    @Autowired
    public CustomPlanSectionApi(CustomPlanSectionService customPlanSectionService) {
        this.customPlanSectionService = customPlanSectionService;
    }

    @Sessions.Uncheck
    @ApiOperation("添加定制板块(后台)")
    @ApiImplicitParam(paramType = ApiParamType.BODY, name = "customPlanSection", value = "定制计划板块", dataType = "CustomPlanSection", required = true)
    @PostMapping("/custom-plan-sections")
    public ResponseEntity create(@Valid @RequestBody CustomPlanSection customPlanSection) {
        log.debug("添加定制板块\t param:{}", customPlanSection);

        // 添加定制板块
        Tips tips = customPlanSectionService.addCustomPlanSection(customPlanSection);
        if (tips.err()) {
            return ResponseEntity.badRequest().body(tips.getMessage());
        }
        Long customPlanSectionId = Long.valueOf(tips.getMessage());
        return customPlanSectionId > 0
                ? ResponseEntity.created(URI.create("/custom-plan-sections/" + customPlanSectionId)).body(Maps.of("id", customPlanSectionId))
                : ResponseEntity.badRequest().body("添加定制板块失败!");
    }

    @Sessions.Uncheck
    @ApiOperation("修改定制板块(后台)")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = ApiParamType.PATH, name = "id", value = "定制板块id", dataType = "Long", required = true),
            @ApiImplicitParam(paramType = ApiParamType.BODY, name = "customPlanSection", value = "定制板块", dataType = "CustomPlanSection", required = true)
    })
    @PutMapping("/custom-plan-sections/{id}")
    public ResponseEntity update(@PathVariable("id") Long id, @Valid @RequestBody CustomPlanSection customPlanSection) {
        log.debug("修改定制板块\t param:{}", customPlanSection);

        customPlanSection.setId(id);
        return customPlanSectionService.update(customPlanSection) ? ResponseEntity.ok().build() : ResponseEntity.badRequest().body("修改定制板块失败!");
    }

    @Sessions.Uncheck
    @ApiOperation(value = "根据id查找单个定制板块(后台)", response = CustomPlanSection.class)
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = ApiParamType.PATH, name = "id", value = "定制板块id", dataType = "Long", required = true),
            @ApiImplicitParam(paramType = ApiParamType.QUERY, name = "flag", value = "是否查询关联定制计划信息", dataType = "Boolean")
    })
    @GetMapping("/custom-plan-sections/{id}")
    public ResponseEntity findById(@PathVariable("id") Long id, @RequestParam(value = "flag", required = false) boolean flag) {
        log.debug("根据id查找单个定制板块\t param:{}", id, flag);

        CustomPlanSection customPlanSection = customPlanSectionService.findById(id, flag);
        return ResponseEntity.ok().body(customPlanSection);
    }

    @Sessions.Uncheck
    @ApiOperation("根据ids删除定制板块(后台)")
    @ApiImplicitParam(paramType = ApiParamType.PATH, name = "ids", value = "多个定制板块id以英文逗号分隔", dataType = "String", required = true)
    @DeleteMapping("/custom-plan-sections/{ids}")
    public ResponseEntity batchDelete(@PathVariable("ids") String ids) {
        log.debug("批量删除定制板块\t param:{}", ids);

        Tips tips = customPlanSectionService.batchDeleteByIds(ids);
        return tips.err() ? ResponseEntity.badRequest().body(tips.getMessage()) : ResponseEntity.noContent().build();
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


    /**
     * 定制计划板块-定制首页
     */
    @Sessions.Uncheck
    @GetMapping("/custom-plan-sections")
    @ApiOperation(value = "查询定制计划板块列表页（定制板块对应定制计划列表页）")
    public ResponseEntity<Tuple<CustomPlanSection>> customPlanSectionTuple() {
        Tuple<CustomPlanSection> productSectionResult = customPlanSectionService.customPlanSectionTuple();
        return ResponseEntity.ok(productSectionResult);
    }

    /**
     * 定制计划信息-定制板块页
     */
    @Sessions.Uncheck
    @PostMapping("/custom-plan-sections/{id}/custom-plans")
    @ApiOperation(value = "查询定制计划板块列表页（定制计划板块和该板块对应的分页定制计划列表）")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = ApiParamType.PATH, name = "id", value = "定制板块id", dataType = "Long", required = true),
            @ApiImplicitParam(paramType = ApiParamType.BODY, name = "planSectionsParam", value = "定制板块", dataType = "PlanSectionsParam", required = true)
    })
    public ResponseEntity<CustomPlanSection> findById(@PathVariable Long id, @RequestBody PlanSectionsParam planSectionsParam) {
        planSectionsParam.setId(id);
        return ResponseEntity.ok(customPlanSectionService.findComPlanSectionId(planSectionsParam));
    }
}