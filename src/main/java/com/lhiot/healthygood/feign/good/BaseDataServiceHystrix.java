package com.lhiot.healthygood.feign.good;

import com.lhiot.healthygood.domain.common.LocationParam;
import com.lhiot.healthygood.domain.good.ProductParam;
import com.lhiot.healthygood.domain.good.ProductSpecificationParam;
import com.lhiot.healthygood.domain.store.Store;
import com.lhiot.healthygood.entity.ApplicationType;
import org.springframework.http.ResponseEntity;

public class BaseDataServiceHystrix implements BaseDataServiceFeign{
    @Override
    public ResponseEntity single(Long productId) {
        return null;
    }

    @Override
    public ResponseEntity search(ProductParam param) {
        return null;
    }

    @Override
    public ResponseEntity singleSpecification(Long specificationId) {
        return null;
    }

    @Override
    public ResponseEntity search(ProductSpecificationParam param) {
        return null;
    }

    @Override
    public ResponseEntity singleShelf(Long shelfId) {
        return null;
    }

    @Override
    public ResponseEntity<Store> findPositionLately(LocationParam param, ApplicationType applicationType) {
        return null;
    }

    @Override
    public ResponseEntity<Store> findStoreById(Long shelfId) {
        return null;
    }
}
