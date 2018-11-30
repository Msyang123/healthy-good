package com.lhiot.healthygood.service.common;

import com.leon.microx.util.Position;
import com.lhiot.healthygood.domain.common.LocationParam;
import com.lhiot.healthygood.domain.store.Store;
import com.lhiot.healthygood.domain.store.StoreResult;
import com.lhiot.healthygood.feign.BaseDataServiceFeign;
import com.lhiot.healthygood.type.ApplicationType;
import com.lhiot.healthygood.util.ConvertAddressToLacation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

@Service
@Transactional
public class CommonService {
    private final BaseDataServiceFeign baseDataServiceFeign;

    @Autowired
    public CommonService(BaseDataServiceFeign baseDataServiceFeign) {
        this.baseDataServiceFeign = baseDataServiceFeign;
    }

    @NonNull
    public StoreResult nearStore(String address) {
        Map<String, Object> locations = ConvertAddressToLacation.fromAddress(address);
        if (Objects.isNull(locations)) {
            return null;
        }
        Double lng = Double.valueOf(String.valueOf(locations.get("lng")));
        Double lat = Double.valueOf(String.valueOf(locations.get("lat")));
        LocationParam locationParam = new LocationParam();
        locationParam.setLat(Double.valueOf(lat));
        locationParam.setLng(Double.valueOf(lng));
        Store store = baseDataServiceFeign.findPositionLately(locationParam, ApplicationType.FRUIT_DOCTOR).getBody();
        StoreResult storeResult = new StoreResult();
        storeResult.setStoreId(store.getId());
        storeResult.setStoreCode(storeResult.getStoreCode());
        storeResult.setStoreName(storeResult.getStoreName());

        Position.GCJ02 gcj02 = Position.GCJ02.of(lng, lat);
        BigDecimal distance = gcj02.distance(Position.GCJ02.of(store.getLongitude().doubleValue(), store.getLatitude().doubleValue()));
        storeResult.setDistance(distance.doubleValue());
        return storeResult;
    }

}
