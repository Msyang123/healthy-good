package com.lhiot.healthygood.service.common;

import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.lhiot.healthygood.feign.BaseDataServiceFeign;
import com.lhiot.healthygood.feign.model.Store;
import com.lhiot.healthygood.feign.model.StoreSearchParam;
import com.lhiot.healthygood.feign.type.ApplicationType;
import com.lhiot.healthygood.util.ConvertAddressToLacation;
import com.lhiot.healthygood.util.FeginResponseTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Transactional
@Slf4j
public class CommonService {
    private final BaseDataServiceFeign baseDataServiceFeign;

    @Autowired
    public CommonService(BaseDataServiceFeign baseDataServiceFeign) {
        this.baseDataServiceFeign = baseDataServiceFeign;
    }

    @NonNull
    public Pages<Store> nearStore(String address) {
        Map<String, Object> locationMap = ConvertAddressToLacation.fromAddress(address);
        if (Objects.isNull(locationMap)) {
            return null;
        }
        String[] locations= ((Map<String,String>)((List)locationMap.get("geocodes")).get(0)).get("location").split(",");
        StoreSearchParam storeSearchParam =new StoreSearchParam();
        storeSearchParam.setApplicationType(ApplicationType.HEALTH_GOOD);
        storeSearchParam.setDistance(10000);//TODO 10km范围内
        storeSearchParam.setLat(Double.valueOf(locations[0]));
        storeSearchParam.setLng(Double.valueOf(locations[1]));
        ResponseEntity<Pages<Store>> storeEntry = baseDataServiceFeign.searchStores(storeSearchParam);
        Tips<Pages<Store>> tips = FeginResponseTools.convertResponse(storeEntry);
        if (tips.err()){
            log.error("查找门店失败{}",tips);
            return null;
        }
        return tips.getData();
    }

}
