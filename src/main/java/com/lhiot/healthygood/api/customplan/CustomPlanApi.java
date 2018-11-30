package com.lhiot.healthygood.api.customplan;

import com.leon.microx.web.session.Sessions;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanDetailResult;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanSectionResult;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanSpecificationDetailResult;
import com.lhiot.healthygood.domain.customplan.model.PlanSectionsParam;
import com.lhiot.healthygood.service.customplan.CustomPlanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @PostMapping("/custom-plan-sections")
    @ApiOperation(value = "查询定制计划板块列表页（定制板块对应定制计划列表页）")
    public ResponseEntity<List<CustomPlanSectionResult>> findByPositionCode(@RequestParam String code){
        List<CustomPlanSectionResult> productSectionResult = customPlanService.findComPlanSectionByCode(code);
        return ResponseEntity.ok(productSectionResult);
    }
    /**
     * 定制计划信息-定制板块页
     */
    @Sessions.Uncheck
    @PostMapping("/custom-plan-sections/{id}/custom-plans")
    @ApiOperation(value = "查询定制计划板块列表页（定制计划板块和该板块对应的分页定制计划列表）")
    public ResponseEntity<CustomPlanSectionResult> findById(@PathVariable Long id,@RequestBody PlanSectionsParam planSectionsParam){
        planSectionsParam.setId(id);

        CustomPlanSectionResult productSectionResult = customPlanService.findComPlanSectionId(planSectionsParam);
        return ResponseEntity.ok(productSectionResult);
    }

    /**
     * 定制计划详细信息
     */
    @Sessions.Uncheck
    @GetMapping("/custom-plans/{id}")
    @ApiOperation(value = "定制计划详细信息（定制计划详细信息页面）")
    public ResponseEntity<CustomPlanDetailResult> customPlans(@PathVariable Long id){
        CustomPlanDetailResult customPlanDetailResult = customPlanService.findDetail(id);
        return ResponseEntity.ok(customPlanDetailResult);
    }

    /**
     * 定制计划详细信息
     */
    @Sessions.Uncheck
    @GetMapping("/custom-plans-specification/{specificationId}")
    @ApiOperation(value = "定制计划详细信息（定制计划详细信息页面）")
    public ResponseEntity<CustomPlanSpecificationDetailResult> specificationDetail(@PathVariable Long specificationId){
        CustomPlanSpecificationDetailResult customPlanSpecificationDetailResult = customPlanService.findCustomPlanSpecificationDetail(specificationId);
        return ResponseEntity.ok(customPlanSpecificationDetailResult);
    }
}
