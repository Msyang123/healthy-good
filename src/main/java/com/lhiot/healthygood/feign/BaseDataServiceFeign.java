package com.lhiot.healthygood.feign;

import com.leon.microx.web.result.Pages;
import com.lhiot.healthygood.feign.model.*;
import com.lhiot.healthygood.feign.type.ApplicationType;
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
    @RequestMapping(value="/products/{id}",method = RequestMethod.GET)
    ResponseEntity<Product> single(@PathVariable("id") Long productId);

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
    ResponseEntity<ProductShelf> singleShelf(@PathVariable("id") Long shelfId,@RequestParam("includeProduct") boolean includeProduct);


    /**
     * 根据条件分页查询商品上架信息列表
     * @param param
     * @return
     */
    @RequestMapping(value="/product-shelves/pages",method = RequestMethod.POST)
    ResponseEntity<Pages<ProductShelf>> searchProductShelves(@RequestBody ProductShelfParam param);

    /**
     * 根据位置查询门店所有列表根据距离排序
     */
    @RequestMapping(value = "/stores/search",method = RequestMethod.POST)
    ResponseEntity<Pages<Store>> searchStores(@RequestBody StoreSearchParam param);

    /**
     * 依据门店id查询门店信息
     * @param shelfId
     * @return
     */
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


    /**
     * 依据门店编码查询门店信息
     * @param code
     * @return
     */
    @RequestMapping(value="/stores/code/{code}",method = RequestMethod.GET)
    ResponseEntity<Store> findStoreByCode(@PathVariable("code") String code,@RequestParam("applicationType") ApplicationType applicationType);

    /**
     * 根据条件分页查询文章信息列表
     * @param param
     * @return
     */
    @RequestMapping(value = "/articles/pages",method = RequestMethod.POST)
    ResponseEntity<Pages<Article>> searchArticle(@RequestBody ArticleParam param);

    /**
     * 根据Id查找文章
     * @param id
     * @return
     */
    @RequestMapping(value="/articles/{id}",method = RequestMethod.GET)
    ResponseEntity<Article> singleArticle(@PathVariable("id") Long id,@RequestParam("addReadAmount") boolean addReadAmount);


    /**
     * 添加版块与商品上架关系
     * @param productSectionRelation
     * @return
     */
    @PostMapping(value = "/product-section-relations")
    ResponseEntity create(@RequestBody ProductSectionRelation productSectionRelation);


    /**
     * 批量删除版块与商品上架关系
     * @param sectionId
     * @param shelfIds
     * @return
     */
    @DeleteMapping("/product-section-relations/batches")
    ResponseEntity deleteBatch(@RequestParam("sectionId") Long sectionId, @RequestParam(value = "shelfIds", required = false) String shelfIds);

    /**
     * 根据条件分页查询位置信息列表
     * @param param
     * @return
     */
    @RequestMapping(value = "/ui-positions/pages",method = RequestMethod.POST)
    ResponseEntity<Pages<UiPosition>> searchUiPosition(@RequestBody UiPositionParam param);

    /**
     * 查询文章版块信息列表
     * @param param
     * @return
     */
    @RequestMapping(value = "/article-sections/pages",method = RequestMethod.POST)
    ResponseEntity<Pages<ArticleSection>> searchArticleSection(@RequestBody ArticleSectionParam param);

}
