package com.lhiot.healthygood.service.common;

import com.leon.microx.web.result.Pages;
import com.lhiot.healthygood.domain.store.StoreResult;
import com.lhiot.healthygood.feign.BaseDataServiceFeign;
import com.lhiot.healthygood.feign.model.Store;
import com.lhiot.healthygood.feign.model.StoreSearchParam;
import com.lhiot.healthygood.type.ApplicationType;
import com.lhiot.healthygood.util.ConvertAddressToLacation;
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
    public StoreResult nearStore(String address) {
        Map<String, Object> locationMap = ConvertAddressToLacation.fromAddress(address);
        if (Objects.isNull(locationMap)) {
            return null;
        }
        String[] locations= ((Map<String,String>)((List)locationMap.get("geocodes")).get(0)).get("location").split(",");
        StoreSearchParam storeSearchParam =new StoreSearchParam();
        storeSearchParam.setApplicationType(ApplicationType.FRUIT_DOCTOR);
        storeSearchParam.setDistance(10);//10km范围内
        storeSearchParam.setLat(Double.valueOf(locations[0]));
        storeSearchParam.setLng(Double.valueOf(locations[1]));
        ResponseEntity<Pages<Store>> storeEntry = baseDataServiceFeign.searchStores(storeSearchParam);
        if (Objects.isNull(storeEntry)||storeEntry.getStatusCode().isError()){
            log.error("查找门店失败{}",storeEntry);
            return null;
        }
        Pages<Store> storeEntryPages = storeEntry.getBody();
        if (storeEntryPages.getTotal()<1){
            log.error("未找到符合条件的门店");
            return null;
        }
        StoreResult storeResult = new StoreResult();
        storeResult.setStoreId(storeEntryPages.getArray().get(0).getId());
        storeResult.setStoreCode(storeEntryPages.getArray().get(0).getCode());
        storeResult.setStoreName(storeEntryPages.getArray().get(0).getName());

        storeResult.setDistance(storeEntryPages.getArray().get(0).getDistance());
        return storeResult;
    }

}
