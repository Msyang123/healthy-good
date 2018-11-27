package com.lhiot.healthygood.service.common;

import com.lhiot.healthygood.domain.common.LocationParam;
import com.lhiot.healthygood.domain.common.StoreResult;
import com.lhiot.healthygood.domain.store.Store;
import com.lhiot.healthygood.entity.ApplicationType;
import com.lhiot.healthygood.feign.good.BaseDataServiceFeign;
import com.lhiot.healthygood.util.BaiduMapUtil;
import com.lhiot.healthygood.util.MapUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
public class CommonService {
    private final BaseDataServiceFeign baseDataServiceFeign;
    @Autowired
    public CommonService(BaseDataServiceFeign baseDataServiceFeign ){
        this.baseDataServiceFeign = baseDataServiceFeign;
    }

     public StoreResult nearStore(String address){
        Map<String,Object> locations = BaiduMapUtil.getLocation(address);
        String lng = locations.get("lng")+"";
        String lat = locations.get("lat")+"";
        LocationParam locationParam = new LocationParam();
        locationParam.setLat(Double.valueOf(lat));
        locationParam.setLng(Double.valueOf(lng));
        Store store = baseDataServiceFeign.findPositionLately(locationParam, ApplicationType.FRUIT_DOCTOR).getBody();
        StoreResult storeResult = new StoreResult();
        storeResult.setStoreId(store.getId());
        storeResult.setStoreCode(storeResult.getStoreCode());
        storeResult.setStoreName(storeResult.getStoreName());
        storeResult.setDistance(MapUtil.getDistance(lat,lng,store.getStorePosition().getLat()+"",store.getStorePosition().getLng()+""));
        return storeResult;
    }

}
