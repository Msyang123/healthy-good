package com.lhiot.healthygood.type;

import lombok.Getter;

public enum BacklogEnum {

	APPLICATION("申请成为鲜果师","请保持电话畅通过，可能会进行电话沟通","审核中"),
	APPLICATION_SUCCESS("申请成为鲜果师","如有疑问请致电0731-85240088","审核成功"),
	APPLICATION_FAILURE("申请成为鲜果师","如有疑问请致电0731-85240088","审核失败");
	
	@Getter
	String backlog;
	
	@Getter
	String remark;
	
	@Getter
	String status;
	
	BacklogEnum(String backlog, String remark, String status){
		this.backlog = backlog;
		this.remark = remark;
		this.status = status;
	}
}
