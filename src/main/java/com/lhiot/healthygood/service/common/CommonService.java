package com.lhiot.healthygood.service.common;

import com.leon.microx.util.Position;
import com.leon.microx.util.StringUtils;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.lhiot.healthygood.config.HealthyGoodConfig;
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

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class CommonService {
    private final BaseDataServiceFeign baseDataServiceFeign;
    private final HealthyGoodConfig.DeliverConfig deliverConfig;

    @Autowired
    public CommonService(BaseDataServiceFeign baseDataServiceFeign, HealthyGoodConfig healthyGoodConfig) {
        this.baseDataServiceFeign = baseDataServiceFeign;
        this.deliverConfig = healthyGoodConfig.getDeliver();
    }

    /**
     * 依据位置获取最近门店
     *
     * @param address 优先使用地址
     * @param lng     如果地址为空使用
     * @param lat     如果地址为空使用
     * @return
     */
    @NonNull
    public Pages<Store> nearStore(String address, Double lng, Double lat) {
        Position.GCJ02 location = null;
        if (StringUtils.isNotBlank(address)) {
            location = this.getPositionFromAddres(address);
            if (Objects.isNull(location)) {
                location = Position.GCJ02.of(lng, lat);
            }
        } else {
            location = Position.GCJ02.of(lng, lat);
        }

        StoreSearchParam storeSearchParam = new StoreSearchParam();
        storeSearchParam.setApplicationType(ApplicationType.HEALTH_GOOD);
        storeSearchParam.setDistance(deliverConfig.getDistance());//距离读取配置
        storeSearchParam.setLat(location.getLatitude());
        storeSearchParam.setLng(location.getLongitude());
        ResponseEntity<Pages<Store>> storeEntry = baseDataServiceFeign.searchStores(storeSearchParam);
        Tips<Pages<Store>> tips = FeginResponseTools.convertResponse(storeEntry);
        if (tips.err()) {
            log.error("查找门店失败{}", tips);
            return null;
        }
        return tips.getData();
    }

    /**
     * 依据地址转换成经纬度
     *
     * @param address
     * @return
     */
    @NonNull
    public Position.GCJ02 getPositionFromAddres(String address) {
        Map<String, Object> locationMap = ConvertAddressToLacation.fromAddress(address);
        if (Objects.isNull(locationMap)) {
            return null;
        }
        String[] locations = ((Map<String, String>) ((List) locationMap.get("geocodes")).get(0)).get("location").split(",");
        return Position.GCJ02.of(Double.valueOf(locations[1]), Double.valueOf(locations[0]));
    }

}
