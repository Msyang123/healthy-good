package com.lhiot.healthygood.api.customplan;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 定制计划api /custom-plan-sections
 */
@Api(description = "定制计划接口")
@Slf4j
@RestController
public class CustomPlanApi {
//    /**
//     * 定制计划信息
//     */
//    @GetMapping("/custom-plan-sections")
//    @ApiOperation(value = "查询定制计划板块列表页（定制板块对应定制计划列表页）")
//    public ResponseEntity<ProductSectionSubResult> findByPositionName(@RequestParam String code){
//        ProductSectionSubResult productSectionResult = productSectionService.findSubByPositionName(positionName,applyType,storeId);
//        return ResponseEntity.ok(productSectionResult);
//    }
}
