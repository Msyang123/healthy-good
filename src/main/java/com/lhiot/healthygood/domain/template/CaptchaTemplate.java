package com.lhiot.healthygood.domain.template;

import lombok.Getter;

/**
 * 验证码短信模版
 *
 * @author Leon (234239150@qq.com) created in 8:43 18.9.15
 */
public enum CaptchaTemplate {

    /**
     * 注册验证码短信模版
     */
    REGISTER("from-register"),
    /**
     * 登录验证码短信模版
     */
    LOGIN("from-login"),
    /**
     * 改密验证码短信模版
     */
    UPDATE_PASSWORD("update-password"),
    /**
     * 身份验证短信模版
     */
    AUTHENTICATION("authentication");

    @Getter
    private String name;

    CaptchaTemplate(String name) {
        this.name = name;
    }
}
