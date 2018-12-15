package com.lhiot.healthygood.api.customplan;

import com.leon.microx.util.Maps;
import com.leon.microx.web.result.Tips;
import com.leon.microx.web.session.Sessions;
import com.leon.microx.web.swagger.ApiHideBodyProperty;
import com.leon.microx.web.swagger.ApiParamType;
import com.lhiot.healthygood.domain.customplan.CustomPlanSectionRelation;
import com.lhiot.healthygood.service.customplan.CustomPlanSectionRelationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

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
    @ApiOperation("批量修改定制版块与定制计划关系(后台)")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = ApiParamType.QUERY, name = "sectionId", value = "定制版块Id", dataType = "Long", required = true),
            @ApiImplicitParam(paramType = ApiParamType.QUERY, name = "planIds", value = "多个定制计划Id以英文逗号分隔", dataType = "String"),
            @ApiImplicitParam(paramType = ApiParamType.QUERY, name = "sorts", value = "多个排序以英文逗号分隔", dataType = "String")
    })
    @PutMapping("/custom-plan-section-relations/batches")
    public ResponseEntity updateBatch(@RequestParam("sectionId") Long sectionId, @RequestParam(value = "planIds", required = false) String planIds,
                                      @RequestParam(value = "sorts", required = false) String sorts) {
        log.debug("批量修改定制版块与定制计划关系\t sectionId: {}, planIds: {},param:{}", sectionId, planIds, sorts);

        Tips tips = customPlanSectionRelationService.updateRelationList(sectionId, planIds, sorts);
        return tips.err() ? ResponseEntity.badRequest().body("批量修改定制版块与定制计划关系失败！") : ResponseEntity.ok().build();
    }

    @Sessions.Uncheck
    @ApiOperation("添加定制版块与定制计划关系(后台)")
    @PostMapping("/custom-plan-section-relations")
    @ApiHideBodyProperty("id")
    public ResponseEntity create(@RequestBody CustomPlanSectionRelation customPlanSectionRelation) {
        log.debug("批量修改定制版块与定制计划关系\t param:{}", customPlanSectionRelation);

        Tips tips = customPlanSectionRelationService.addRelation(customPlanSectionRelation);
        if (tips.err()) {
            return ResponseEntity.badRequest().body(tips.getMessage());
        }
        Long relationId = Long.valueOf(tips.getMessage());
        return relationId > 0
                ? ResponseEntity.created(URI.create("/custom-plan-section-relations/" + relationId)).body(Maps.of("id", relationId))
                : ResponseEntity.badRequest().body("添加商品与版块关系记录失败！");
    }


    @Sessions.Uncheck
    @ApiOperation("根据关联Id删除定制版块与定制计划架关系(后台)")
    @ApiImplicitParam(paramType = ApiParamType.PATH, name = "id", value = "关系Id", dataType = "Long", required = true)
    @DeleteMapping("/custom-plan-section-relations/{id}")
    public ResponseEntity delete(@PathVariable("id") Long relationId) {
        log.debug("根据关联Id删除定制版块与定制计划架关系\t relationId: {}", relationId);

        return customPlanSectionRelationService.deleteRelation(relationId.toString()) ? ResponseEntity.noContent().build() : ResponseEntity.badRequest().body("根据关联Id删除定制版块与定制计划架关系失败！");
    }

    @Sessions.Uncheck
    @ApiOperation("批量添加定制版块与定制计划架关系(后台)")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = ApiParamType.QUERY, name = "sectionId", value = "商品版块Id", dataType = "Long", required = true),
            @ApiImplicitParam(paramType = ApiParamType.QUERY, name = "planIds", value = "多个定制计划Id以英文逗号分隔", dataType = "String", required = true),
            @ApiImplicitParam(paramType = ApiParamType.QUERY, name = "sorts", value = "多个排序以英文逗号分隔", dataType = "String", required = true)
    })
    @PostMapping("/custom-plan-section-relations/batches")
    public ResponseEntity createBatch(@RequestParam("sectionId") Long sectionId, @RequestParam("planIds") String planIds, @RequestParam("sorts") String sorts) {
        log.debug("根据关联Id删除定制版块与定制计划架关系\t sectionId:{}, planIds{}, sorts:{}", sectionId, planIds, sorts);

        Tips tips = customPlanSectionRelationService.addRelationList(sectionId, planIds, sorts);
        return tips.err() ? ResponseEntity.badRequest().body(tips.getMessage()) : ResponseEntity.ok().build();
    }

    @Sessions.Uncheck
    @ApiOperation("批量删除定制版块与定制计划架关系(后台)")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = ApiParamType.QUERY, name = "sectionId", value = "商品版块Id", dataType = "Long", required = true),
            @ApiImplicitParam(paramType = ApiParamType.QUERY, name = "planIds", value = "多个商品上架Id以英文逗号分隔,为空则删除此版块所有上架关系", dataType = "String")
    })
    @DeleteMapping("/custom-plan-section-relations/batches")
    public ResponseEntity deleteBatch(@RequestParam("sectionId") Long sectionId, @RequestParam(value = "planIds", required = false) String planIds) {
        log.debug("根据关联Id删除定制版块与定制计划架关系\t sectionId:{}, planIds:{}", sectionId, planIds);

        return customPlanSectionRelationService.deleteRelationList(sectionId, planIds) ? ResponseEntity.noContent().build() : ResponseEntity.badRequest().body("删除信息失败！");
    }
}