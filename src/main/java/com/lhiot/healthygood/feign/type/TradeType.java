package com.lhiot.healthygood.feign.type;

public enum TradeType {
    /**
     * 支付宝原生支付
     */
    ALI_APP("QUICK_MSECURITY_PAY", Void.class),
    /**
     * 支付宝手机网页支付
     */
    ALI_WAP("QUICK_WAP_WAY", Void.class),

    /**
     * 微信公众号支付( openid 必传，为用户在商户appid下的唯一标识)
     */
    WX_JS_API("JSAPI", Void.class),

    /**
     * 微信原生扫码支付( product_id 必传，为二维码中包含的商品ID，商户自行定义)
     */
    WX_NATIVE("NATIVE", Void.class),

    /**
     * 微信APP支付
     */
    WX_APP("APP", Void.class),

    /**
     * 微信刷卡支付
     */
    WX_MICRO_PAY("MICROPAY", Void.class),

    /**
     * 其他方式支付（例如余额支付）
     */
    OTHER_PAY("OTHER", Void.class);

    private String source;
    private Class<?> target;

    TradeType(String source, Class<?> target) {
        this.source = source;
    }
}
