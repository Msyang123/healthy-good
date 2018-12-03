package com.lhiot.healthygood.feign;

import com.leon.microx.web.result.Pages;
import com.lhiot.healthygood.feign.model.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

/**
 * 基础数据服务
 */
@Component
@FeignClient(value = "BASIC-DATA-SERVICE-V1-0")
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
    ResponseEntity searchProducts(@RequestBody ProductParam param);

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
    ResponseEntity searchSpecifications(@RequestBody ProductSpecificationParam param);

    /**
     * 根据Id查找商品上架
     * @param shelfId
     * @return
     */
    @RequestMapping(value="/product-shelves/{id}",method = RequestMethod.GET)
    ResponseEntity<ProductShelf> singleShelf(@PathVariable("id") Long shelfId);

    /**
     * 根据位置查询门店所有列表根据距离排序
     */
    @RequestMapping(value = "/stores/search",method = RequestMethod.POST)
    ResponseEntity<Pages<Store>> searchStores(@RequestBody StoreSearchParam param);

    @RequestMapping(value="/stores/{id}",method = RequestMethod.GET)
    ResponseEntity<Store> findStoreById(@PathVariable("id") Long shelfId);

    /**
     * 根据条件分页查询广告信息列表
     * @param param
     * @return
     */
    @RequestMapping(value = "/advertisements/pages",method = RequestMethod.POST)
    ResponseEntity<Pages<Advertisement>> searchAdvertisementPages(@RequestBody AdvertisementParam param);

    /**
     * 根据Id查找商品版块
     * @param sectionId
     * @param includeShelves
     * @param includeShelvesQty
     * @return
     */
    @RequestMapping(value = "/product-sections/{id}",method = RequestMethod.GET)
    ResponseEntity<ProductSection> singleProductSection(@PathVariable("id") Long sectionId,
                          @RequestParam(value = "includeShelves", required = false) boolean includeShelves,
                          @RequestParam(value = "includeShelvesQty", required = false) Long includeShelvesQty);

    /**
     * 根据条件分页查询商品版块信息列表
     * @param param
     * @return
     */
    @RequestMapping(value = "/product-sections/pages",method = RequestMethod.POST)
    ResponseEntity<Pages<ProductSection>> searchProductSection(@RequestBody ProductSectionParam param);


}
