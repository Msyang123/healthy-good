package com.lhiot.healthygood.wechat;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = WeChatProperties.PROPERTIES_PREFIX)
public class WeChatProperties {

    public static final String PROPERTIES_PREFIX = "fruit-doctor-mall.wechat";
    /**
     * 编码
     */
    private String UTF8 = "UTF-8";
    /**
     * 发送验证码的第三方推送服务地址
     */
    private InetRemoteUrl sendSms;

    /**
     * 验证发送验证码服务地址
     */
    private InetRemoteUrl validateSms;
    /**
     * http连接超时（毫秒数）
     */
    private Integer httpConnectionTimeoutExpress = -1;

    private WeChatOauth weChatOauth;

    @Data
    public static final class WeChatOauth {
        /**
         * APPID
         */
        private String appId;
        /**
         * APP密钥
         */
        private String appSecret;

        /**
         * 授权后跳转到的地址
         */
        private String appRedirectUri;

        /**
         * 调整到前端页面的地址
         */
        private String appFrontUri;
    }

    @Data
    public static class InetRemoteUrl {
        private String url;
        private String version;

        public <T> HttpEntity<T> createRequest(T parameters) {
            return this.createRequest(parameters, null);
        }

        public <T> HttpEntity<T> createRequest(T parameters, Map<String, String> extHeaders) {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            headers.setAccept(Arrays.asList(MediaType.ALL, MediaType.APPLICATION_JSON_UTF8));
            headers.set("version", version);
            if (!CollectionUtils.isEmpty(extHeaders)) {
                extHeaders.forEach(headers::set);
            }
            return new HttpEntity<>(parameters, headers);
        }
    }
}
