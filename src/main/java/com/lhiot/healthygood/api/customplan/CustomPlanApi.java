package com.lhiot.healthygood.api.customplan;

import com.leon.microx.util.Maps;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.leon.microx.web.session.Sessions;
import com.leon.microx.web.swagger.ApiParamType;
import com.lhiot.healthygood.domain.customplan.CustomPlan;
import com.lhiot.healthygood.domain.customplan.CustomPlanProduct;
import com.lhiot.healthygood.domain.customplan.CustomPlanSpecification;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanDetailResult;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanParam;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanResult;
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
     * 定制计划规格信息
     */
    @Sessions.Uncheck
    @GetMapping("/custom-plans-specification/{specificationId}")
    @ApiOperation(value = "定制计划规格信息（创建定制计划订单信息页面）")
    @ApiImplicitParam(paramType = ApiParamType.PATH, name = "specificationId", value = "定制计划规格id", dataType = "Long", required = true)
    public ResponseEntity<CustomPlanSpecification> specificationDetail(@PathVariable Long specificationId) {
        CustomPlanSpecification customPlanSpecification = customPlanService.findCustomPlanSpecificationDetail(specificationId);
        return ResponseEntity.ok(customPlanSpecification);
    }

    @Sessions.Uncheck
    @ApiOperation("添加定制计划(后台)")
    @ApiImplicitParam(paramType = ApiParamType.BODY, name = "customPlanDetailResult", value = "定制计划", dataType = "CustomPlanDetailResult", required = true)
    @PostMapping("/custom-plans")
    public ResponseEntity create(@Valid @RequestBody CustomPlanDetailResult customPlanDetailResult) {
        log.debug("添加定制计划\t param:{}", customPlanDetailResult);

        Tips tips = customPlanService.addCustomPlan(customPlanDetailResult);
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
            @ApiImplicitParam(paramType = ApiParamType.BODY, name = "customPlanDetailResult", value = "定制计划", dataType = "CustomPlanDetailResult", required = true)
    })
    @PutMapping("/custom-plans/{id}")
    public ResponseEntity update(@PathVariable("id") Long id, @Valid @RequestBody CustomPlanDetailResult customPlanDetailResult) {
        log.debug("修改定制计划\t param:{}", customPlanDetailResult);


        return customPlanService.update(id, customPlanDetailResult) ? ResponseEntity.ok().build() : ResponseEntity.badRequest().body(Tips.warn("修改信息失败!"));
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
        List<CustomPlanSpecification> customPlanSpecifications = customPlanResult.getCustomPlanSpecifications().stream().collect(Collectors.toList());
        //TODO 不是在定制规格中添加定制商品 不是在规格中包含定制商品
        /*customPlanSpecifications.forEach(customPlanSpecification ->
                customPlanProducts.addAll(customPlanSpecification.getCustomPlanProducts().stream().peek(customPlanProduct -> customPlanProduct.setPlanId(id)).collect(Collectors.toList())));*/
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
