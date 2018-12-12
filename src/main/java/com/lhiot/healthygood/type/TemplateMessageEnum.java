package com.lhiot.healthygood.type;

import lombok.Getter;

public enum TemplateMessageEnum {

	MEMBER_SHIP("会员加入提醒","9ehsO00SfJrMFh8BVEXE7LlTjEuj2S8KEF2MPScfWXw","",""),
	PURCHASE_NOTICE("购买成功通知","YrOXNEn-TcH3qHNL4-vjdcNe5SzAlGusqMZ87Zz3-d8","https://health.food-see.com/weixin/order/detail/",""),
	APPLY_FRUIT_DOCTOR("申请鲜果师","r3BxOwDyu3RNeWSOjvonT4ezaBGoXos-A0P3RBpqmUI","",""),
	NOTICE_OF_PRESENTATION("提现申请通知","P6G0reCkG4igJourVrHBZklrzIQFg1wbdPCfyDo8T08","",""),
	ORDER_REMINDING("订单状态变更通知","YGIpgCSkS3U5m-Kzk-aRw2QDywwb-Vqr814Z7IsGerQ","https://health.food-see.com/weixin/order/detail/",""),
	UPGRADE_FRUIT_DOCTOR("推荐上明星鲜果师通知","9ehsO00SfJrMFh8BVEXE7LlTjEuj2S8KEF2MPScfWXw","","");
	
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
