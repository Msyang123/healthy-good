package com.lhiot.healthygood.type;

import lombok.Getter;

public enum TemplateMessageEnum {

	MEMBER_SHIP("会员加入提醒","4Js8sBqdVH6cOg3J--1URQ65xShGuqglRvhLIDfkoi8","",""),
	PURCHASE_NOTICE("购买成功通知","Kq90zxLVQwgrz2LLA7u9ZL2gYUdYlsUd-dMcpe0GRdY","https://health.food-see.com/weixin/order/detail/",""),
	APPLY_FRUIT_DOCTOR("申请鲜果师","QVEui2alNQ4idRXX8gyG2eWKI7jD1infNQD4TX0oC0k","",""),
	NOTICE_OF_PRESENTATION("提现申请通知","g4WEmNYhoez_Xh3Q656nRCC59uTbIA65c_ZcSbxum2M","",""),
	ORDER_REMINDING("订单状态变更通知","mJdHozupHuFS0yQwSZH3MIkejgjO3qwtyArZ93sVCv0","https://health.food-see.com/weixin/order/detail/",""),
	UPGRADE_FRUIT_DOCTOR("推荐上明星鲜果师通知","QVEui2alNQ4idRXX8gyG2eWKI7jD1infNQD4TX0oC0k","","");
	
	@Getter
	String templateId;
	
	@Getter
	String templateName;
	
	@Getter
	String url;
	
	@Getter
	String doctorUrl;
	
	TemplateMessageEnum(String templateName,String templateId,String url,String doctorUrl){
		this.templateId = templateId;
		this.templateName = templateName;
		this.url = url;
		this.doctorUrl = doctorUrl;
	}
}
