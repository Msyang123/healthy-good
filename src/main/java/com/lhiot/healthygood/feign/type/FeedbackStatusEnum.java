package com.lhiot.healthygood.feign.type;

import lombok.Getter;

public enum FeedbackStatusEnum {
	UNREPLY("未回复"),
	REPLY("已回复且用户未读"),
	READED("已读");
    @Getter
    private String description;

    FeedbackStatusEnum(String description) {
        this.description = description;
    }
}
