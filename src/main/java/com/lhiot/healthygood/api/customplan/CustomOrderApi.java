package com.lhiot.healthygood.api.customplan;

import com.leon.microx.web.result.Tips;
import com.leon.microx.web.session.Sessions;
import com.lhiot.healthygood.domain.customplan.CustomOrder;
import com.lhiot.healthygood.service.customplan.CustomOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Api(description = "购买定制计划")
@Slf4j
@RestController
public class CustomOrderApi {

    private final CustomOrderService customOrderService;

    @Autowired
    public CustomOrderApi(CustomOrderService customOrderService) {

        this.customOrderService = customOrderService;
    }

    @PostMapping("/custom-orders")
    @ApiOperation(value = "立即定制-创建定制计划")
    public ResponseEntity create(@Valid @RequestBody CustomOrder customOrder, Sessions.User user) {
        Long userId = Long.valueOf(user.getUser().get("userId").toString());
        customOrder.setUserId(userId);
        Tips result = customOrderService.createCustomOrder(customOrder);
        if (result.err()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }


}
