package com.lhiot.healthygood.api.customplan;

import com.leon.microx.web.session.Sessions;
import com.lhiot.healthygood.domain.customplan.CustomPlanDetailResult;
import com.lhiot.healthygood.domain.customplan.CustomPlanSectionResult;
import com.lhiot.healthygood.service.customplan.CustomPlanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
     * 定制计划信息
     */
    @Sessions.Uncheck
    @GetMapping("/custom-plan-sections")
    @ApiOperation(value = "查询定制计划板块列表页（定制板块对应定制计划列表页）")
    public ResponseEntity<List<CustomPlanSectionResult>> findByPositionCode(@RequestParam String code){
        List<CustomPlanSectionResult> productSectionResult = customPlanService.findComPlanSectionByCode(code);
        return ResponseEntity.ok(productSectionResult);
    }
    /**
     * 定制计划信息
     */
    @Sessions.Uncheck
    @GetMapping("/custom-plan-sections/{id}/custom-plans")
    @ApiOperation(value = "查询定制计划板块列表页（定制板块对应定制计划列表页）")
    public ResponseEntity<CustomPlanSectionResult> findByPositionCode(@PathVariable Long id){
        CustomPlanSectionResult productSectionResult = customPlanService.findComPlanSectionId(id);
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

}
