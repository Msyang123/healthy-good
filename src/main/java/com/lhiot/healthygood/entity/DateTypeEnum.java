package com.lhiot.healthygood.entity;

import lombok.Getter;

public enum DateTypeEnum {
	DAY("日"),
	WEEK("周"),
	MONTH("月"),
	QUARTER("季度");
	
	@Getter
	private final String displayTag;
	
	DateTypeEnum(String displayTag){
		this.displayTag = displayTag;
	}
}
