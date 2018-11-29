package com.lhiot.healthygood.api.customplan;

import com.leon.microx.web.session.Sessions;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(description = "购买定制计划")
@Slf4j
@RestController
public class CustomOrderApi {

    @Autowired
    public CustomOrderApi(){

    }

    @Sessions.Uncheck
    @PostMapping("/custom-orders")
    @ApiOperation(value = "立即定制-创建定制计划")
    public ResponseEntity create(){
        return null;
    }


}
