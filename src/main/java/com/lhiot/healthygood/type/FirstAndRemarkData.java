package com.lhiot.healthygood.type;

import lombok.Getter;

public enum FirstAndRemarkData {

	MEMBER_SHIP_USER("欢迎加入和色果膳这个大家庭",""),
	MEMBER_SHIP_DOCTOR("恭喜您，您的好友已成功加入",""),
	
	PURCHASE_NOTICE_USER("","感谢您的光临！"),
	PURCHASE_NOTICE_DOCTOR("您的好友下单成功，您获得了@BALANCE@果币",""),
	
	APPLY("恭喜您提交申请成功，我们将会在1-3个工作日内完成审核",""),
	APPLY_SUCCESS("恭喜您申请鲜果师成功！",""),
	APPLY_FAILURE("您好，您的申请被驳回，请重新提交申请！",""),
	
	NOTICE_OF_PRESENTATION("您好，您已发起提现申请","您的提现申请已被受理，提现金额将在1-5个工作日到账。"),
	
	ORDER_DISPATCHING("您好，您的订单正在配送中","点击查看详情"),
	ORDER_RETURNNING("您好，您的退货申请已提交成功","点击查看详情"),
	ORDER_ALREADY_RETURN("您好，您的订单已退货成功","购买金额会原路退回到您的账户，如有疑问请致电：0731-85240088"),
	
	UPGRADE_FRUIT_DOCTOR("恭喜您已经被推荐为明星鲜果师！","");
	
	@Getter
	String firstData;
	
	@Getter
	String remarkData;
	
	FirstAndRemarkData(String firstData,String remarkData){
		this.firstData = firstData;
		this.remarkData = remarkData;
	}
}
