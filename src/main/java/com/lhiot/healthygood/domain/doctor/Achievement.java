package com.lhiot.healthygood.domain.doctor;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@ApiModel(description = "鲜果师业绩统计")
@NoArgsConstructor
public class Achievement {
	
	@ApiModelProperty(notes="销售总金额",dataType="Long")
	private Long salesAmount;
	
	@ApiModelProperty(notes="订单总数",dataType="Long")
	private Long orderCount;
	
	@ApiModelProperty(notes="本月销售总金额",dataType="Long")
	private Long salesAmountOfThisMonth;
	
	@ApiModelProperty(notes="本月订单总数",dataType="Long")
	private Long orderCountOfThisMonth;
	
	@ApiModelProperty(notes="业绩收入",dataType="Long")
	private Long incomeAmount;
	
	@ApiModelProperty(notes="总销售额",dataType="Long")
	private Long summaryAmount;

	@ApiModelProperty(notes = "头像", dataType = "String")
	private String avatar;

	@ApiModelProperty(notes = "备注", dataType = "String")
	private String description;
}
