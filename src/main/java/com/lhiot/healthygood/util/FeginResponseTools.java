package com.lhiot.healthygood.util;

import com.leon.microx.web.result.Tips;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

@Slf4j
public class FeginResponseTools {

    public static <T> Tips<T> convertResponse(ResponseEntity<T> responseEntity){

        if(Objects.isNull(responseEntity)||responseEntity.getStatusCode().isError()){
            log.error("调用基础服务失败:{}",responseEntity);
            return Tips.warn((Objects.isNull(responseEntity)?"调用基础服务失败":responseEntity.getBody().toString()));
        }
        return new Tips<T>().data(responseEntity.getBody());
    }
}
