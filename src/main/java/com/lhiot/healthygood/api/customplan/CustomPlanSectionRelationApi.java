package com.lhiot.healthygood.api.customplan;

import com.leon.microx.web.session.Sessions;
import com.leon.microx.web.swagger.ApiParamType;
import com.lhiot.healthygood.domain.customplan.CustomPlanSectionRelation;
import com.lhiot.healthygood.service.customplan.CustomPlanSectionRelationService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author hufan created in 2018/11/27 14:42
 **/
@Api(description = "定制版块与定制计划关系接口")
@Slf4j
@RestController
public class CustomPlanSectionRelationApi {
    private final CustomPlanSectionRelationService customPlanSectionRelationService;

    @Autowired
    public CustomPlanSectionRelationApi(CustomPlanSectionRelationService customPlanSectionRelationService) {
        this.customPlanSectionRelationService = customPlanSectionRelationService;
    }

    @Sessions.Uncheck
    @ApiOperation("添加定制版块与定制计划关系")
    @ApiImplicitParam(paramType = ApiParamType.BODY, name = "customPlanSectionRelation", value = "定制版块与定制计划关系信息", dataType = "CustomPlanSectionRelation", required = true)
    @PostMapping("/custom-plan-section-relations")
    public ResponseEntity create(@RequestBody CustomPlanSectionRelation customPlanSectionRelation) {
        Long relationId = customPlanSectionRelationService.addRelation(customPlanSectionRelation);
        return relationId > 0 ?
                ResponseEntity.ok().build()
                : ResponseEntity.badRequest().body("添加定制计划与定制版块关系记录失败！");
    }


    @Sessions.Uncheck
    @ApiOperation("根据Id删除定制版块与定制计划关系")
    @ApiImplicitParam(paramType = ApiParamType.PATH, name = "id", value = "关系Id", dataType = "Long", required = true)
    @DeleteMapping("/custom-plan-section-relations/{id}")
    public ResponseEntity delete(@PathVariable("id") Long relationId) {
        return customPlanSectionRelationService.deleteRelation(relationId) ? ResponseEntity.noContent().build() : ResponseEntity.badRequest().body("删除信息失败！");
    }


    @Sessions.Uncheck
    @ApiOperation("批量添加定制版块与定制计划关系")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = ApiParamType.QUERY, name = "sectionId", value = "定制版块Id", dataType = "Long", required = true),
            @ApiImplicitParam(paramType = ApiParamType.QUERY, name = "planIds", value = "多个定制计划Id以英文逗号分隔", dataType = "String", required = true),
            @ApiImplicitParam(paramType = ApiParamType.QUERY, name = "sorts", value = "多个排序以英文逗号分隔", dataType = "String", required = true)
    })
    @PostMapping("/custom-plan-section-relations/batches")
    public ResponseEntity createBatch(@RequestParam("sectionId") Long sectionId, @RequestParam("planIds") String planIds, @RequestParam("sorts") String sorts) {
        return customPlanSectionRelationService.addRelationList(sectionId, planIds, sorts) ? ResponseEntity.ok().build() : ResponseEntity.badRequest().body("批量添加定制版块与定制计划关系失败！");
    }


    @Sessions.Uncheck
    @ApiOperation("批量删除定制版块与定制计划关系")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = ApiParamType.QUERY, name = "sectionId", value = "定制版块Id", dataType = "Long", required = true),
            @ApiImplicitParam(paramType = ApiParamType.QUERY, name = "planIds", value = "多个定制计划Id以英文逗号分隔,为空则删除此定制版块所有上架关系", dataType = "String")
    })
    @DeleteMapping("/custom-plan-section-relations/batches")
    public ResponseEntity deleteBatch(@RequestParam("sectionId") Long sectionId, @RequestParam(value = "planIds", required = false) String planIds) {
        return customPlanSectionRelationService.deleteRelationList(sectionId, planIds) ? ResponseEntity.noContent().build() : ResponseEntity.badRequest().body("删除信息失败！");
    }

    @Sessions.Uncheck
    @ApiModelProperty("根据板块id")
    public ResponseEntity findBySectionId(Long sectionId) {
        return ResponseEntity.ok().build();
    }
}