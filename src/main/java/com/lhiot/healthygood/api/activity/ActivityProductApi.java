package com.lhiot.healthygood.api.activity;

import com.leon.microx.util.Maps;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.leon.microx.web.session.Sessions;
import com.leon.microx.web.swagger.ApiParamType;
import com.lhiot.healthygood.domain.activity.ActivityProduct;
import com.lhiot.healthygood.domain.activity.model.ActivityProductParam;
import com.lhiot.healthygood.domain.activity.model.ActivityProductResult;
import com.lhiot.healthygood.service.activity.ActivityProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;

/**
 * @author hufan created in 2018/12/3 15:44
 **/
@Api(description = "新品尝鲜活动商品接口")
@Slf4j
@RestController
public class ActivityProductApi {
    private final ActivityProductService activityProductService;

    public ActivityProductApi(ActivityProductService activityProductService) {
        this.activityProductService = activityProductService;
    }

    @Sessions.Uncheck
    @ApiOperation("添加新品尝鲜活动商品(后台)")
    @ApiImplicitParam(paramType = ApiParamType.BODY, name = "activityProduct", value = "新品尝鲜活动商品", dataType = "ActivityProduct", required = true)
    @PostMapping("/activity-products")
    public ResponseEntity create(@Valid @RequestBody ActivityProduct activityProduct) {
        log.debug("添加新品尝鲜活动商品\t param:{}", activityProduct);

        Tips tips = activityProductService.create(activityProduct);
        if (tips.err()) {
            return ResponseEntity.badRequest().body(Tips.warn(tips.getMessage()));
        }
        Long activityProductId = Long.valueOf(tips.getMessage());
        return activityProductId > 0 ?
                ResponseEntity.created(URI.create("/activity-products/" + activityProductId)).body(Maps.of("id", activityProductId)) :
                ResponseEntity.badRequest().body(Tips.warn("添加新品尝鲜活动商品失败!"));
    }

    @Sessions.Uncheck
    @ApiOperation("修改新品尝鲜活动商品(后台)")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = ApiParamType.PATH, name = "id", value = "新品尝鲜活动商品id", dataType = "Long", required = true),
            @ApiImplicitParam(paramType = ApiParamType.BODY, name = "activityProduct", value = "新品尝鲜活动商品", dataType = "ActivityProduct", required = true)
    })
    @PutMapping("/activity-products/{id}")
    public ResponseEntity update(@PathVariable("id") Long id, @Valid @RequestBody ActivityProduct activityProduct) {
        log.debug("修改新品尝鲜活动商品\t param:{}", activityProduct);

        Tips tips = activityProductService.updateById(id,activityProduct);
        return !tips.err() ? ResponseEntity.ok().build() : ResponseEntity.badRequest().body(Tips.warn("修改信息失败!"));
    }

    @Sessions.Uncheck
    @ApiOperation("根据ids删除新品尝鲜活动商品(后台)")
    @ApiImplicitParam(paramType = ApiParamType.PATH, name = "ids", value = "多个新品尝鲜活动商品id以英文逗号分隔", dataType = "String", required = true)
    @DeleteMapping("/activity-products/{ids}")
    public ResponseEntity batchDelete(@PathVariable("ids") String ids) {
        log.debug("批量删除新品尝鲜活动商品\t param:{}", ids);

        Tips tips = activityProductService.batchDeleteByIds(ids);
        return !tips.err() ? ResponseEntity.noContent().build() : ResponseEntity.badRequest().body(Tips.warn(tips.getMessage()));
    }

    @Sessions.Uncheck
    @ApiOperation(value = "根据条件分页查询新品尝鲜活动商品信息列表(后台)", response = ActivityProductResult.class, responseContainer = "Set")
    @ApiImplicitParam(paramType = ApiParamType.BODY, name = "param", value = "查询条件", dataType = "ActivityProductParam")
    @PostMapping("/activity-products/pages")
    public ResponseEntity search(@RequestBody ActivityProductParam param) {
        log.debug("根据条件分页查询新品尝鲜活动商品信息列表\t param:{}", param);

        Pages<ActivityProductResult> pages = activityProductService.findList(param);
        return ResponseEntity.ok(pages);
    }
}