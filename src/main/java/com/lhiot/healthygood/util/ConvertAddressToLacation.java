/**
 * Copyright © 2016 SGSL
 * 湖南绿航恰果果农产品有限公司
 * http://www.sgsl.com
 * All rights reserved.
 */
package com.lhiot.healthygood.util;

import com.leon.microx.util.Jackson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Objects;

/**
 * 地址转换成经纬度
 *
 * @author yj
 * @version 1.0  2018年11月29日下午2:39:59
 */
@Slf4j
public class ConvertAddressToLacation {

    private static final String GD_KEY = "0a846fab5fd453615cd027875855482b";

    private static final String GD_MAP = "https://restapi.amap.com/v3/geocode/geo?key=" + GD_KEY + "&city=0731&address=";

    //   public static void main(String[] args) {
    //System.out.println(getDistance("28.236447","112.914096","28.123026","113.021028"));
//		JSONObject J = GD_ADDRESS("麓谷企业广场");
//		System.out.println(J.getJSONArray("geocodes").getJSONObject(0).get("location"));
//	}

    @NonNull
    public static Map<String, Object> fromAddress(String address) {
        try {
            URL url = new URL(GD_MAP + address);
            URLConnection httpsConn = url.openConnection();
            if (httpsConn != null) {

                InputStream inputStream = httpsConn.getInputStream();
                String result = InputSteamToString.fromInputStream(inputStream);
                if (Objects.nonNull(result)) {
                    return Jackson.map(result);
                }
                return null;
            }
        } catch (IOException ignore) {
            log.error("convertRequestParameters", ignore);
        }
        return null;
    }
}
