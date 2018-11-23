package com.lhiot.healthygood.entity;

import lombok.Getter;

public enum PeriodType {
	current("本期"),
	last("上期");
	
	@Getter
	private final String displayTag;
	
	PeriodType(String displayTag){
		this.displayTag = displayTag;
	}
}
