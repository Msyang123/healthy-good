package com.lhiot.healthygood.feign.good;

import com.lhiot.healthygood.domain.common.LocationParam;
import com.lhiot.healthygood.domain.good.ProductParam;
import com.lhiot.healthygood.domain.good.ProductShelf;
import com.lhiot.healthygood.domain.good.ProductSpecificationParam;
import com.lhiot.healthygood.domain.store.Store;
import com.lhiot.healthygood.entity.ApplicationType;
import com.lhiot.healthygood.feign.user.BaseUserServerHystrix;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 基础数据服务
 */
@FeignClient(value = "BASIC-DATA-SERVICE-V1-0-HUFAN", fallback = BaseUserServerHystrix.class)
public interface BaseDataServiceFeign {
    /**
     * 根据id查询商品
     */
    @RequestMapping(value="/products/{id}",method = RequestMethod.POST)
    ResponseEntity single(@PathVariable("id") Long productId);

    /**
     * 根据条件分页查询商品信息列表
     */
    @RequestMapping(value="/products/pages",method = RequestMethod.POST)
    ResponseEntity search(@RequestBody ProductParam param);

    /**
     * 根据Id查找商品规格
     * @param specificationId
     * @return
     */
    @RequestMapping(value="/product-specifications/{id}",method = RequestMethod.GET)
    ResponseEntity singleSpecification(@PathVariable("id") Long specificationId);


    /**
     * 根据条件分页查询商品规格信息列表
     * @param param
     * @return
     */
    @RequestMapping(value="/product-specifications/pages",method = RequestMethod.POST)
    ResponseEntity search(@RequestBody ProductSpecificationParam param);

    /**
     * 根据Id查找商品上架
     * @param shelfId
     * @return
     */
    @RequestMapping(value="/product-shelves/{id}",method = RequestMethod.GET)
    ResponseEntity<ProductShelf> singleShelf(@PathVariable("id") Long shelfId);

    @RequestMapping(value="/position/lately",method = RequestMethod.GET)
    ResponseEntity<Store> findPositionLately(@RequestParam("param") LocationParam param,@RequestParam("applicationType") ApplicationType applicationType);


}
