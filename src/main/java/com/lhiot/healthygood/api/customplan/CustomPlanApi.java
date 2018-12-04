package com.lhiot.healthygood.api.customplan;

import com.leon.microx.util.Maps;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.leon.microx.web.result.Tuple;
import com.leon.microx.web.session.Sessions;
import com.leon.microx.web.swagger.ApiParamType;
import com.lhiot.healthygood.domain.customplan.CustomPlan;
import com.lhiot.healthygood.domain.customplan.CustomPlanProduct;
import com.lhiot.healthygood.domain.customplan.CustomPlanSection;
import com.lhiot.healthygood.domain.customplan.model.*;
import com.lhiot.healthygood.service.customplan.CustomPlanService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 定制计划api /custom-plan-sections
 */
@Api(description = "定制计划接口")
@Slf4j
@RestController
public class CustomPlanApi {
    private final CustomPlanService customPlanService;


    @Autowired
    public CustomPlanApi(CustomPlanService customPlanService) {
        this.customPlanService = customPlanService;
    }

    /**
     * 定制计划板块-定制首页
     */
    @Sessions.Uncheck
    @GetMapping("/custom-plan-sections")
    @ApiOperation(value = "查询定制计划板块列表页（定制板块对应定制计划列表页）")
    public ResponseEntity<Tuple<CustomPlanSection>> customPlanSectionTuple() {
        Tuple<CustomPlanSection> productSectionResult = customPlanService.customPlanSectionTuple();
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
        CustomPlanSection productSectionResult = customPlanService.findComPlanSectionId(planSectionsParam);
        return ResponseEntity.ok(productSectionResult);
    }

    /**
     * 定制计划详细信息
     */
    @Sessions.Uncheck
    @GetMapping("/custom-plans/{id}")
    @ApiOperation(value = "定制计划详细信息（定制计划详细信息页面）", response = CustomPlanDetailResult.class)
    @ApiImplicitParam(paramType = ApiParamType.PATH, name = "id", value = "定制计划id", dataType = "Long", required = true)
    public ResponseEntity customPlans(@PathVariable Long id) {
        CustomPlanDetailResult customPlanDetailResult = customPlanService.findDetail(id);
        return ResponseEntity.ok(customPlanDetailResult);
    }

    /**
     * 定制计划详细信息
     */
    @Sessions.Uncheck
    @GetMapping("/custom-plans-specification/{specificationId}")
    @ApiOperation(value = "定制计划详细信息（定制计划详细信息页面）")
    @ApiImplicitParam(paramType = ApiParamType.PATH, name = "specificationId", value = "定制计划规格id", dataType = "Long", required = true)
    public ResponseEntity<CustomPlanSpecificationDetailResult> specificationDetail(@PathVariable Long specificationId) {
        CustomPlanSpecificationDetailResult customPlanSpecificationDetailResult = customPlanService.findCustomPlanSpecificationDetail(specificationId);
        return ResponseEntity.ok(customPlanSpecificationDetailResult);
    }

    @Sessions.Uncheck
    @ApiOperation("添加定制计划(后台)")
    @ApiImplicitParam(paramType = ApiParamType.BODY, name = "customPlanResult", value = "定制计划", dataType = "CustomPlanResult", required = true)
    @PostMapping("/custom-plans")
    public ResponseEntity create(@Valid @RequestBody CustomPlanResult customPlanResult) {
        log.debug("添加定制计划\t param:{}", customPlanResult);

        Tips tips = customPlanService.addCustomPlan(customPlanResult);
        if (tips.err()) {
            return ResponseEntity.badRequest().body(Tips.warn(tips.getMessage()));
        }
        Long customPlanId = Long.valueOf(tips.getMessage());
        return customPlanId > 0 ?
                ResponseEntity.created(URI.create("/custom-plans/" + customPlanId)).body(Maps.of("id", customPlanId)) :
                ResponseEntity.badRequest().body(Tips.warn("添加定制计划失败!"));
    }

    @Sessions.Uncheck
    @ApiOperation("修改定制计划(后台)")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = ApiParamType.PATH, name = "id", value = "定制计划id", dataType = "Long", required = true),
            @ApiImplicitParam(paramType = ApiParamType.BODY, name = "customPlan", value = "定制计划", dataType = "CustomPlan", required = true)
    })
    @PutMapping("/custom-plans/{id}")
    public ResponseEntity update(@PathVariable("id") Long id, @Valid @RequestBody CustomPlan customPlan) {
        log.debug("修改定制计划\t param:{}", customPlan);

        customPlan.setId(id);
        return customPlanService.update(customPlan) ? ResponseEntity.ok().build() : ResponseEntity.badRequest().body(Tips.warn("修改信息失败!"));
    }

    @Sessions.Uncheck
    @ApiOperation("修改定制计划商品(后台)")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = ApiParamType.PATH, name = "id", value = "定制计划id", dataType = "Long", required = true),
            @ApiImplicitParam(paramType = ApiParamType.BODY, name = "customPlanResult", value = "定制计划商品", dataType = "CustomPlanResult", required = true)
    })
    @PutMapping("/custom-plan-product/{id}")
    public ResponseEntity updateProduct(@PathVariable("id") Long id, @RequestBody CustomPlanResult customPlanResult) {
        log.debug("修改定制计划\t param:{}", customPlanResult);

        List<CustomPlanProduct> customPlanProducts = new ArrayList<>();
        List<CustomPlanSpecificationResult> customPlanSpecifications = customPlanResult.getCustomPlanSpecifications().stream().collect(Collectors.toList());
        customPlanSpecifications.forEach(customPlanSpecification ->
                customPlanProducts.addAll(customPlanSpecification.getCustomPlanProducts().stream().peek(customPlanProduct -> customPlanProduct.setPlanId(id)).collect(Collectors.toList())));
        Tips tips = customPlanService.updateProduct(customPlanProducts);
        return !tips.err() ? ResponseEntity.ok().build() : ResponseEntity.badRequest().body(Tips.warn("修改信息失败!"));
    }

    @Sessions.Uncheck
    @ApiOperation("根据ids删除定制计划(后台)")
    @ApiImplicitParam(paramType = ApiParamType.PATH, name = "ids", value = "多个定制计划id以英文逗号分隔", dataType = "String", required = true)
    @DeleteMapping("/custom-plans/{ids}")
    public ResponseEntity batchDelete(@PathVariable("ids") String ids) {
        log.debug("批量删除定制计划\t param:{}", ids);

        Tips tips = customPlanService.batchDeleteByIds(ids);
        return !tips.err() ? ResponseEntity.noContent().build() : ResponseEntity.badRequest().body(Tips.warn(tips.getMessage()));
    }

    @Sessions.Uncheck
    @ApiOperation(value = "根据条件分页查询定制计划信息列表(后台)", response = CustomPlanResult.class, responseContainer = "Set")
    @ApiImplicitParam(paramType = ApiParamType.BODY, name = "param", value = "查询条件", dataType = "CustomPlanParam")
    @PostMapping("/custom-plans/pages")
    public ResponseEntity search(@RequestBody CustomPlanParam param) {
        log.debug("根据条件分页查询定制计划信息列表\t param:{}", param);

        Pages<CustomPlanResult> pages = customPlanService.findList(param);
        return ResponseEntity.ok(pages);
    }
}
