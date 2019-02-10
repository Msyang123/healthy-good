package com.lhiot.healthygood.api.customplan;

import com.leon.microx.util.Maps;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.leon.microx.web.session.Sessions;
import com.leon.microx.web.swagger.ApiHideBodyProperty;
import com.lhiot.healthygood.domain.customplan.CustomPlanSpecification;
import com.lhiot.healthygood.domain.customplan.CustomPlanSpecificationStandard;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanSpecificationStandardParam;
import com.lhiot.healthygood.mapper.customplan.CustomPlanSpecificationMapper;
import com.lhiot.healthygood.service.customplan.CustomPlanSpecificationStandardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Description:定制计划规格基础数据接口类
 *
 * @author hufan
 * @date 2018/12/08
 */
@Api(description = "定制计划规格基础数据接口")
@Slf4j
@RestController
public class CustomPlanSpecificationStandardApi {

    private final CustomPlanSpecificationStandardService customPlanSpecificationStandardService;
    private final CustomPlanSpecificationMapper customPlanSpecificationMapper;

    @Autowired
    public CustomPlanSpecificationStandardApi(CustomPlanSpecificationStandardService customPlanSpecificationStandardService, CustomPlanSpecificationMapper customPlanSpecificationMapper) {
        this.customPlanSpecificationStandardService = customPlanSpecificationStandardService;
        this.customPlanSpecificationMapper = customPlanSpecificationMapper;
    }

    @Sessions.Uncheck
    @PostMapping("/custom-plan-specification-standards")
    @ApiOperation(value = "添加定制计划规格基础数据(后台)")
    @ApiHideBodyProperty({"id"})
    public ResponseEntity create(@RequestBody CustomPlanSpecificationStandard customPlanSpecificationStandard) {
        log.debug("添加定制计划规格基础数据\t param:{}", customPlanSpecificationStandard);

        Long id = customPlanSpecificationStandardService.create(customPlanSpecificationStandard);
        return id > 0
                ? ResponseEntity.created(URI.create("/custom-plan-specification-standards/" + id)).body(Maps.of("id", id))
                : ResponseEntity.badRequest().body("添加定制计划规格基础数据失败！");
    }

    @Sessions.Uncheck
    @PutMapping("/custom-plan-specification-standards/{id}")
    @ApiOperation(value = "根据id更新定制计划规格基础数据(后台)")
    @ApiHideBodyProperty({"id"})
    public ResponseEntity update(@PathVariable("id") Long id, @RequestBody CustomPlanSpecificationStandard customPlanSpecificationStandard) {
        log.debug("根据id更新定制计划规格基础数据\t id:{} param:{}", id, customPlanSpecificationStandard);

        Tips tips = customPlanSpecificationStandardService.updateById(id, customPlanSpecificationStandard);
        if (tips.err()) {
            return ResponseEntity.badRequest().body(tips.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    @Sessions.Uncheck
    @DeleteMapping("/custom-plan-specification-standards/{ids}")
    @ApiOperation(value = "根据ids删除定制计划规格基础数据(后台)")
    @ApiImplicitParam(paramType = "path", name = "ids", value = "要删除定制计划规格基础数据的ids,逗号分割", required = true, dataType = "String")
    public ResponseEntity deleteByIds(@PathVariable("ids") String ids) {
        log.debug("根据ids删除定制计划规格基础数据\t param:{}", ids);

        // 如果已经关联了定制板块规格，则不能删除
        List<CustomPlanSpecification> customPlanSpecificationList = this.customPlanSpecificationMapper.selectByStandardsIds(Arrays.asList(ids.split(",")));
        if (!CollectionUtils.isEmpty(customPlanSpecificationList)) {
            List<Long> idList = customPlanSpecificationList.stream().map(CustomPlanSpecification::getStandardId).distinct().collect(Collectors.toList());
            return ResponseEntity.badRequest().body("以下id:" + idList + "已经关联了定制板块规格，删除失败！");
        }
        return customPlanSpecificationStandardService.deleteByIds(ids) ? ResponseEntity.noContent().build() : ResponseEntity.badRequest().body("删除定制计划规格基础数据失败！");
    }

    @Sessions.Uncheck
    @GetMapping("/custom-plan-specification-standards/{id}")
    @ApiOperation(value = "根据id查询定制计划规格基础数据(后台)", notes = "根据id查询定制计划规格基础数据", response = CustomPlanSpecificationStandard.class)
    @ApiImplicitParam(paramType = "path", name = "id", value = "主键id", required = true, dataType = "Long")
    public ResponseEntity findCustomPlanSpecificationStandard(@PathVariable("id") Long id) {
        log.debug("根据id查询定制计划规格基础数据\t param:{}", id);

        CustomPlanSpecificationStandard customPlanSpecificationStandard = customPlanSpecificationStandardService.selectById(id);
        return ResponseEntity.ok().body(customPlanSpecificationStandard);
    }

    @Sessions.Uncheck
    @ApiOperation(value = "查询定制计划规格基础数据分页列表(后台)", response = CustomPlanSpecificationStandard.class, responseContainer = "Set")
    @PostMapping("/custom-plan-specification-standards/pages")
    public ResponseEntity search(@RequestBody CustomPlanSpecificationStandardParam customPlanSpecificationStandardParam) {
        log.debug("查询定制计划规格基础数据分页列表\t param:{}", customPlanSpecificationStandardParam);

        Pages<CustomPlanSpecificationStandard> pages = customPlanSpecificationStandardService.pageList(customPlanSpecificationStandardParam);
        return ResponseEntity.ok(pages);
    }

    @Sessions.Uncheck
    @ApiOperation(value = "查询定制计划规格基础数据列表(后台)", response = CustomPlanSpecificationStandard.class, responseContainer = "List")
    @GetMapping("/custom-plan-specification-standards")
    public ResponseEntity findList() {
        log.debug("查询定制计划规格基础数据列表\t param:{}");

        List<CustomPlanSpecificationStandard> list = customPlanSpecificationStandardService.findList();
        return ResponseEntity.ok(list);
    }
}
