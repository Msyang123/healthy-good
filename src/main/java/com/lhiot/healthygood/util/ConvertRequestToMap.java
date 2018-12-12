package com.lhiot.healthygood.util;

import com.leon.microx.util.IOUtils;
import com.leon.microx.util.Jackson;
import com.leon.microx.util.StringUtils;
import com.leon.microx.util.xml.XNode;
import com.leon.microx.util.xml.XReader;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class ConvertRequestToMap {

    /**
     * 将request中流转换成Map参数
     *
     * @param request
     * @return
     */
    @Nullable
    public static Map<String, Object> convertRequestParameters(HttpServletRequest request) {
        Map<String, Object> parameters = null;
        try (InputStream inputStream = request.getInputStream()) {
            if (Objects.nonNull(inputStream)) {
                @Cleanup BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                String parameterString = StringUtils.collectionToDelimitedString(IOUtils.readLines(in), "");
                log.info("request转换成字符串结果：{}", parameterString);
                if (StringUtils.isNotBlank(parameterString)) {
                    parameters = Jackson.map(parameterString);
                }
            }
        } catch (IOException ignore) {
            log.error("convertRequestParameters", ignore);
        }
        return parameters;
    }

    /**
     * 将map转换成Map<String,String>参数
     * @param map
     * @return
     */
    @Nullable
    public static Map<String,String> convertMapToStrMap(Map<String, Object> map){
        if(Objects.isNull(map))
            return null;
        Map<String, String> stringMap =new HashMap<>(map.size());
        map.forEach((key,val)->{
            if (Objects.isNull(val)){
                stringMap.put(key,null);
            }else{
                stringMap.put(key,String.valueOf(val));
            }
        });
        return stringMap;
    }

    /**
     * 微信支付回调转换xml为map<String,String>
     * @param request
     * @return
     */
    public static Map<String,String> convertRequestXmlFormatToMap(HttpServletRequest request){

        try (InputStream inputStream = request.getInputStream()) {
            if (Objects.nonNull(inputStream)) {
                @Cleanup BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                String parameterString = StringUtils.collectionToDelimitedString(IOUtils.readLines(in), "");
                log.info("request转换成字符串结果：{}", parameterString);
                if (StringUtils.isNotBlank(parameterString)) {
                    XReader xpath = XReader.of(parameterString);
                    List<XNode> nodes = xpath.evalNodes("//xml/*");
                    Map<String, String> parameters = new TreeMap();
                    for (XNode node : nodes) {
                        parameters.put(node.tegName(), node.body());
                    }
                    return parameters;
                }
            }
        } catch (IOException ignore) {
            log.error("convertRequestXmlFormatToMap", ignore);
        }
        return null;
    }
}
