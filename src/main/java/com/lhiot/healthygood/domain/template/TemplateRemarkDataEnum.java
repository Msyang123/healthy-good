package com.lhiot.healthygood.domain.template;

import lombok.Getter;

public enum TemplateRemarkDataEnum {

	MEMBER_SHIP("会员加入提醒","4Js8sBqdVH6cOg3J--1URQ65xShGuqglRvhLIDfkoi8","",""),
	PURCHASE_NOTICE("购买成功通知","Kq90zxLVQwgrz2LLA7u9ZL2gYUdYlsUd-dMcpe0GRdY","",""),
	APPLY_FRUIT_DOCTOR("申请鲜果师","QVEui2alNQ4idRXX8gyG2eWKI7jD1infNQD4TX0oC0k","",""),
	NOTICE_OF_PRESENTATION("提现申请通知","g4WEmNYhoez_Xh3Q656nRCC59uTbIA65c_ZcSbxum2M","",""),
	ORDER_REMINDING("最新订单信息提醒","mJdHozupHuFS0yQwSZH3MIkejgjO3qwtyArZ93sVCv0","","");
	
	@Getter
	String templateId;
	
	@Getter
	String templateName;
	
	@Getter
	String url;
	
	@Getter
	String doctorUrl;
	
	TemplateRemarkDataEnum(String templateName,String templateId,String url,String doctorUrl){
		this.templateId = templateId;
		this.templateName = templateName;
		this.url = url;
		this.doctorUrl = doctorUrl;
	}
}
