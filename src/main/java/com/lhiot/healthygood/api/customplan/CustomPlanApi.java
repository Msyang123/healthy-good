package com.lhiot.healthygood.api.customplan;

import com.leon.microx.util.Maps;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.leon.microx.web.session.Sessions;
import com.leon.microx.web.swagger.ApiParamType;
import com.lhiot.dc.dictionary.module.Dictionary;
import com.lhiot.healthygood.domain.customplan.CustomPlan;
import com.lhiot.healthygood.domain.customplan.CustomPlanAndSpecification;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanDetailResult;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanParam;
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
import java.util.List;

/**
 * 定制计划api /custom-plan-sections
 */
@Api(description = "定制计划接口(no session)")
@Slf4j
@RestController
public class CustomPlanApi {
    private final CustomPlanService customPlanService;


    @Autowired
    public CustomPlanApi(CustomPlanService customPlanService) {
        this.customPlanService = customPlanService;
    }

    /**
     * 定制计划详细信息
     */
    @Sessions.Uncheck
    @GetMapping("/custom-plans/{id}")
    @ApiOperation(value = "定制计划详细信息（定制计划详细信息页面）")
    @ApiImplicitParam(paramType = ApiParamType.PATH, name = "id", value = "定制计划id", dataType = "Long", required = true)
    public ResponseEntity<CustomPlanDetailResult> customPlans(@PathVariable Long id) {
        return ResponseEntity.ok(customPlanService.findDetail(id));
    }

    /**
     * 定制计划规格信息
     */
    @Sessions.Uncheck
    @GetMapping("/custom-plans-specification/{specificationId}")
    @ApiOperation(value = "定制计划规格信息（创建定制计划订单信息页面）")
    @ApiImplicitParam(paramType = ApiParamType.PATH, name = "specificationId", value = "定制计划规格id", dataType = "Long", required = true)
    public ResponseEntity<CustomPlanAndSpecification> specificationDetail(@PathVariable Long specificationId) {
        return ResponseEntity.ok(customPlanService.findCustomPlanSpecificationDetail(specificationId));
    }

    @Sessions.Uncheck
    @ApiOperation("添加定制计划(后台)")
    @ApiImplicitParam(paramType = ApiParamType.BODY, name = "customPlanDetailResult", value = "定制计划", dataType = "CustomPlanDetailResult", required = true)
    @PostMapping("/custom-plans")
    public ResponseEntity create(@Valid @RequestBody CustomPlanDetailResult customPlanDetailResult) {
        log.debug("添加定制计划\t param:{}", customPlanDetailResult);

        Tips tips = customPlanService.addCustomPlan(customPlanDetailResult);
        if (tips.err()) {
            return ResponseEntity.badRequest().body(tips.getMessage());
        }
        Long customPlanId = Long.valueOf(tips.getMessage());
        return customPlanId > 0
                ? ResponseEntity.created(URI.create("/custom-plans/" + customPlanId)).body(Maps.of("id", customPlanId))
                : ResponseEntity.badRequest().body("添加定制计划失败!");
    }

    @Sessions.Uncheck
    @ApiOperation("修改定制计划(后台)")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = ApiParamType.PATH, name = "id", value = "定制计划id", dataType = "Long", required = true),
            @ApiImplicitParam(paramType = ApiParamType.BODY, name = "customPlanDetailResult", value = "定制计划", dataType = "CustomPlanDetailResult", required = true)
    })
    @PutMapping("/custom-plans/{id}")
    public ResponseEntity update(@PathVariable("id") Long id, @Valid @RequestBody CustomPlanDetailResult customPlanDetailResult) {
        log.debug("修改定制计划\t id:{} param:{}", id, customPlanDetailResult);

        Tips tips = customPlanService.update(id, customPlanDetailResult);
        return tips.err() ? ResponseEntity.badRequest().body("修改定制计划失败!") : ResponseEntity.ok().build();
    }

    @Sessions.Uncheck
    @ApiOperation("修改定制计划周期类型信息(后台)")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = ApiParamType.PATH, name = "id", value = "定制计划id", dataType = "Long", required = true),
            @ApiImplicitParam(paramType = ApiParamType.BODY, name = "customPlanDetailResult", value = "定制计划", dataType = "CustomPlanDetailResult", required = true)
    })
    @PutMapping("/custom-plan-periods/{id}")
    public ResponseEntity updatePeriod(@PathVariable("id") Long id, @RequestBody CustomPlanDetailResult customPlanDetailResult) {
        log.debug("修改定制计划周期类型信息\t id:{} param:{}", id, customPlanDetailResult);

        Tips tips = customPlanService.updatePeriod(id, customPlanDetailResult);
        return tips.err() ? ResponseEntity.badRequest().body("修改定制计划周期类型信息失败!") : ResponseEntity.ok().build();
    }

    @Sessions.Uncheck
    @ApiOperation("根据ids删除定制计划(后台)")
    @ApiImplicitParam(paramType = ApiParamType.PATH, name = "ids", value = "多个定制计划id以英文逗号分隔", dataType = "String", required = true)
    @DeleteMapping("/custom-plans/{ids}")
    public ResponseEntity batchDelete(@PathVariable("ids") String ids) {
        log.debug("批量删除定制计划\t param:{}", ids);

        Tips tips = customPlanService.batchDeleteByIds(ids);
        return tips.err() ? ResponseEntity.badRequest().body(tips.getMessage()) : ResponseEntity.noContent().build();
    }

    @Sessions.Uncheck
    @ApiOperation(value = "根据条件分页查询定制计划信息列表(后台)", response = CustomPlanDetailResult.class, responseContainer = "Set")
    @ApiImplicitParam(paramType = ApiParamType.BODY, name = "param", value = "查询条件", dataType = "CustomPlanParam")
    @PostMapping("/custom-plans/pages")
    public ResponseEntity search(@RequestBody CustomPlanParam param) {
        log.debug("根据条件分页查询定制计划信息列表\t param:{}", param);

        Pages<CustomPlanDetailResult> pages = customPlanService.findList(param);
        return ResponseEntity.ok(pages);
    }

    @Sessions.Uncheck
    @GetMapping("/custom-plans/image")
    @ApiOperation(value = "获取定制计划套餐配置说明图")
    public ResponseEntity<List<Dictionary.Entry>> customPlanImage() {
        return ResponseEntity.ok(customPlanService.dictionaryOptional("customPlanImage").get().getEntries());
    }

    @Sessions.Uncheck
    @GetMapping("/custom-plans/max-pause")
    @ApiOperation(value = "定制计划最大暂停天数")
    public ResponseEntity<Dictionary> customPlanMaxPauseDay() {
        return ResponseEntity.ok(customPlanService.customPlanMaxPauseDay());
    }

    @Sessions.Uncheck
    @ApiOperation(value = "根据上架商品id查询关联的定制计划列表(后台)", response = CustomPlan.class, responseContainer = "List")
    @ApiImplicitParam(paramType = ApiParamType.PATH, name = "id", value = "上架id", dataType = "Long")
    @GetMapping("/product-shelves/{id}/custom-plans")
    public ResponseEntity findByShelfId(@PathVariable("id") Long id) {
        log.debug("根据上架商品id查询关联的定制计划列表\t param:{}", id);

        List<CustomPlan> customPlanList = customPlanService.findListByShelfId(id);
        return ResponseEntity.ok(customPlanList);
    }

}
