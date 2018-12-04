package com.lhiot.healthygood.config;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import static com.lhiot.healthygood.config.HealthyGoodConfig.PREFIX;

@Data
@ToString
@RefreshScope
@ConfigurationProperties(prefix = PREFIX)
public class HealthyGoodConfig {
    static final String PREFIX = "lhiot.healthy-good";

    private WechatOauthConfig wechatOauth;

    private WechatPayConfig wechatPay;

    private AliPayConfig aliAay;

    private DeliverConfig deliver;

    @Data
    @ToString
    public static final class WechatOauthConfig {
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
    @ToString
    public static class WechatPayConfig {
        private String callbackUrl;
    }

    @Data
    @ToString
    public static class AliPayConfig {
        private String callbackUrl;
    }

    @Data
    @ToString
    public static class DeliverConfig {
        private String type;
        private String backUrl;
    }
}

