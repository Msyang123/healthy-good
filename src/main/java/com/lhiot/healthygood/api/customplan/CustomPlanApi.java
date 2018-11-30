package com.lhiot.healthygood.api.customplan;

import com.leon.microx.util.Maps;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.leon.microx.web.session.Sessions;
import com.leon.microx.web.swagger.ApiParamType;
import com.lhiot.healthygood.domain.customplan.*;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanDetailResult;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanSectionResult;
import com.lhiot.healthygood.service.customplan.CustomPlanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 定制计划api /custom-plan-sections
 */
@Api("定制计划接口")
@Slf4j
@RestController
public class CustomPlanApi {
    private final CustomPlanService customPlanService;


    @Autowired
    public CustomPlanApi(CustomPlanService customPlanService) {
        this.customPlanService = customPlanService;
    }

    /**
     * 定制计划信息
     */
    @Sessions.Uncheck
    @GetMapping("/custom-plan-sections")
    @ApiOperation(value = "查询定制计划板块列表页（定制板块对应定制计划列表页）")
    public ResponseEntity<List<CustomPlanSectionResult>> findByPositionCode(@RequestParam String code) {
        List<CustomPlanSectionResult> productSectionResult = customPlanService.findComPlanSectionByCode(code);
        return ResponseEntity.ok(productSectionResult);
    }

    /**
     * 定制计划信息
     */
    @Sessions.Uncheck
    @GetMapping("/custom-plan-sections/{id}/custom-plans")
    @ApiOperation(value = "查询定制计划板块列表页（定制板块对应定制计划列表页）")
    public ResponseEntity<CustomPlanSectionResult> findByPositionCode(@PathVariable Long id) {
        CustomPlanSectionResult productSectionResult = customPlanService.findComPlanSectionId(id);
        return ResponseEntity.ok(productSectionResult);
    }

    /**
     * 定制计划详细信息
     */
    @Sessions.Uncheck
//    @GetMapping("/custom-plans/{id}")
    @ApiOperation(value = "定制计划详细信息（定制计划详细信息页面）")
    public ResponseEntity<CustomPlanDetailResult> customPlans(@PathVariable Long id) {
        CustomPlanDetailResult customPlanDetailResult = customPlanService.findDetail(id);
        return ResponseEntity.ok(customPlanDetailResult);
    }

    @Sessions.Uncheck
    @ApiOperation(value = "根据条件分页查询定制计划信息列表(后台)", response = CustomPlan.class, responseContainer = "Set")
    @ApiImplicitParam(paramType = ApiParamType.BODY, name = "param", value = "查询条件", dataType = "CustomPlanParam")
    @PostMapping("/custom-plans/pages")
    public ResponseEntity search(@RequestBody CustomPlanParam param) {
        log.debug("根据条件分页查询定制计划信息列表\t param:{}", param);

        Pages<CustomPlan> pages = customPlanService.findList(param);
        return ResponseEntity.ok(pages);
    }

    @Sessions.Uncheck
    @ApiOperation("添加定制计划(后台)")
    @ApiImplicitParam(paramType = ApiParamType.BODY, name = "customPlanResult", value = "定制计划", dataType = "CustomPlanResult", required = true)
    @PostMapping("/custom-plans")
    public ResponseEntity create(@RequestBody CustomPlanResult customPlanResult) {
        log.debug("添加定制计划\t param:{}", customPlanResult);

        Tips tips = customPlanService.addCustomPlan(customPlanResult);
        if (tips.err()) {
            return ResponseEntity.badRequest().body(tips.getMessage());
        }
        Long customPlanId = Long.valueOf(tips.getMessage());
        return customPlanId > 0 ? ResponseEntity.created(URI.create("/custom-plans/" + customPlanId)).body(Maps.of("id", customPlanId)) : ResponseEntity.badRequest().body("添加定制计划失败!");
    }

    @Sessions.Uncheck
    @ApiOperation("修改定制计划(后台)")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = ApiParamType.PATH, name = "id", value = "定制计划id", dataType = "Long", required = true),
            @ApiImplicitParam(paramType = ApiParamType.BODY, name = "customPlan", value = "定制计划", dataType = "CustomPlan", required = true)
    })
    @PutMapping("/custom-plans/{id}")
    public ResponseEntity update(@PathVariable("id") Long id, @RequestBody CustomPlan customPlan) {
        log.debug("修改定制计划\t param:{}", customPlan);

        customPlan.setId(id);
        return customPlanService.update(customPlan) ? ResponseEntity.ok().build() : ResponseEntity.badRequest().body("修改信息失败!");
    }

    @Sessions.Uncheck
    @ApiOperation("修改定制计划商品(后台)")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = ApiParamType.PATH, name = "id", value = "定制计划id", dataType = "Long", required = true),
            @ApiImplicitParam(paramType = ApiParamType.BODY, name = "customPlanResult", value = "定制计划商品", dataType = "CustomPlanResult", required = true)
    })
    @PutMapping("/custom-plan-product/{id}")
    @ApiIgnore("customPlanSectionIds")
    public ResponseEntity updateProduct(@PathVariable("id") Long id, @RequestBody CustomPlanResult customPlanResult) {
        log.debug("修改定制计划\t param:{}", customPlanResult);

        List<CustomPlanProduct> customPlanProducts = new ArrayList<>();
        List<CustomPlanSpecification> customPlanSpecifications = customPlanResult.getCustomPlanSpecifications().stream().collect(Collectors.toList());
        customPlanSpecifications.forEach(customPlanSpecification ->
                customPlanProducts.addAll(customPlanSpecification.getCustomPlanProducts().stream().peek(customPlanProduct -> customPlanProduct.setPlanId(id)).collect(Collectors.toList())));
        Tips tips = customPlanService.updateProduct(customPlanProducts);
        return !tips.err() ? ResponseEntity.ok().build() : ResponseEntity.badRequest().body("修改信息失败!");
    }

    // 已存在查询详情
    @Sessions.Uncheck
    @ApiOperation("根据id查找单个定制计划(后台)")
    @ApiImplicitParam(paramType = ApiParamType.PATH, name = "id", value = "定制计划id", dataType = "Long", required = true)
    @GetMapping("/custom-plans/{id}")
    public ResponseEntity findById(@PathVariable("id") Long id) {
        log.debug("根据id查找单个定制计划\t param:{}", id);

        CustomPlan customPlan = customPlanService.findById(id);
        return ResponseEntity.ok().body(customPlan);
    }

    @Sessions.Uncheck
    @ApiOperation("根据ids删除定制计划(后台)")
    @ApiImplicitParam(paramType = ApiParamType.PATH, name = "ids", value = "多个定制计划id以英文逗号分隔", dataType = "String", required = true)
    @DeleteMapping("/custom-plans/{ids}")
    public ResponseEntity batchDelete(@PathVariable("ids") String ids) {
        log.debug("批量删除定制计划\t param:{}", ids);

        // TODO 改定制计划是否关联定制板块 返回不能删除的定制计划id
        return customPlanService.batchDeleteByIds(ids) ? ResponseEntity.noContent().build() : ResponseEntity.badRequest().body("删除信息失败!");
    }
}
