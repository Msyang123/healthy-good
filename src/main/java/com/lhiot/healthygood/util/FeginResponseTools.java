package com.lhiot.healthygood.util;

import com.leon.microx.web.result.Tips;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

@Slf4j
public class FeginResponseTools {

    /**
     * 获取基础服务调用返回的responseEntity转换成Tips对象
     * @param responseEntity
     * @param <T>
     * @return
     */
    public static <T> Tips<T> convertResponse(ResponseEntity<T> responseEntity){

        if(Objects.isNull(responseEntity)||responseEntity.getStatusCode().isError()){
            log.error("调用基础服务失败:{}",responseEntity);
            return Tips.warn((Objects.isNull(responseEntity)?"服务内部错误":responseEntity.getBody().toString()));
        }
        return new Tips<T>().data(responseEntity.getBody());
    }

    /**
     * 依据tips标准编码将tips转换成对应ResponseEntity对象
     * @param tips
     * @param <T>
     * @return
     */
    public static <T> ResponseEntity<Tips> returnTipsResponse(Tips<T> tips){
        if(tips.err()){
            return ResponseEntity.badRequest().body(tips);
        }
        return ResponseEntity.ok(tips);
    }
}
