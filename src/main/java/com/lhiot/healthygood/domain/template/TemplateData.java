package com.lhiot.healthygood.domain.template;

import lombok.Data;

import java.util.Map;

@Data
public class TemplateData {
	private Map<String,Object> member;
	private Map<String,Object> Doctor;
	private String url;
}
