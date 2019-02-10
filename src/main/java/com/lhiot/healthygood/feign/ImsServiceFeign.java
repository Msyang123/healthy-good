package com.lhiot.healthygood.feign;

import com.lhiot.healthygood.feign.model.ImsOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Component
@FeignClient("IMS-SERVICE-V1-0")
public interface ImsServiceFeign {
    //商城用户访问权限session url集
    @RequestMapping(value = "/ims-operation/mall-user", method = RequestMethod.GET)
    ResponseEntity<List<ImsOperation>> selectAuthority();
}
