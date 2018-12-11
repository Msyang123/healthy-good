package com.lhiot.healthygood.api.advertisement;

import com.leon.microx.predefine.OnOff;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.leon.microx.web.session.Sessions;
import com.lhiot.healthygood.feign.BaseDataServiceFeign;
import com.lhiot.healthygood.feign.model.Advertisement;
import com.lhiot.healthygood.feign.model.AdvertisementParam;
import com.lhiot.healthygood.feign.model.UiPosition;
import com.lhiot.healthygood.feign.model.UiPositionParam;
import com.lhiot.healthygood.util.FeginResponseTools;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(description = "广告类接口")
@Slf4j
@RestController
public class AdvertisementApi {

    private final BaseDataServiceFeign baseDataServiceFeign;

    @Autowired
    public AdvertisementApi(BaseDataServiceFeign baseDataServiceFeign) {
        this.baseDataServiceFeign = baseDataServiceFeign;
    }

    @Sessions.Uncheck
    @ApiOperation(value = "根据位置编码查询广告列表", response = Advertisement.class )
    @GetMapping("/advertisements/position")
    public ResponseEntity searchAdvertisementPages(@RequestParam String code){
        UiPositionParam uiPositionParam = new UiPositionParam();
        uiPositionParam.setApplicationType("HEALTH_GOOD");
        uiPositionParam.setCodes(code);
        ResponseEntity uiPositionEntity = baseDataServiceFeign.searchUiPosition(uiPositionParam);
        Tips<Pages<UiPosition>> tips = FeginResponseTools.convertResponse(uiPositionEntity);
        if (tips.err()){
            return ResponseEntity.badRequest().body(tips);
        }
         Long positionId = tips.getData().getArray().get(0).getId();//TODO 这里需要改
        AdvertisementParam advertisementParam = new AdvertisementParam();
        advertisementParam.setPositionId(positionId);
        advertisementParam.setAdvertiseStatus(OnOff.ON);
        ResponseEntity<Pages<Advertisement>> advertisements = baseDataServiceFeign.searchAdvertisementPages(advertisementParam);
        Tips result = FeginResponseTools.convertResponse(advertisements);
        return FeginResponseTools.returnTipsResponse(result);
    }
}
