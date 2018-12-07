package com.lhiot.healthygood.type;

import lombok.Getter;

public enum FruitDoctorOrderExchange {
	
	FRUIT_DOCTOR_BONUS("鲜果师-计算销售业绩","fruit_doctor_order","fruit_doctor_calculation_bonus"),
	FRUIT_TEMPLATE_MESSAGE("鲜果师-发送模板消息","fruit_doctor_tmp","fruit_doctor_send-tmpessage");
    
	@Getter
	String description;
	
	@Getter
    String exchangeName;

    @Getter
    String queueName;

	FruitDoctorOrderExchange(String description, String exchangeName, String queueName){
		this.description = description;
		this.exchangeName = exchangeName;
		this.queueName = queueName;
	}
}
