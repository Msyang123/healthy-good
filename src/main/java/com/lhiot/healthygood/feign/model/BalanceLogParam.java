package com.lhiot.uc.basic.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lhiot.dc.dictionary.HasEntries;
import com.lhiot.uc.basic.entity.OperationStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author xiaojian  created in  2018/12/9 14:13
 */
@Data
@ApiModel
public class BalanceLogParam {
    @ApiModelProperty(notes = "用户ID", dataType = "Long")
    private Long baseUserId;
    @ApiModelProperty(notes = "加减操作标识：SUBTRACT - 减，ADD-加", dataType = "OperationStatus")
    private OperationStatus operation;
    @ApiModelProperty(notes = "来源", dataType = "String")
    private String sourceType;
    @ApiModelProperty(notes = "来源ID：例如：订单ID，活动ID", dataType = "String")
    private String sourceId;
    @HasEntries(from = "applications")
    @ApiModelProperty(notes = "应用类型", dataType = "String")
    private String applicationType;
    @ApiModelProperty(notes = "起始创建时间", dataType = "Date")
    private Date beginCreateAt;
    @ApiModelProperty(notes = "截止创建时间", dataType = "Date")
    private Date endCreateAt;
    @ApiModelProperty(notes = "每页查询条数(为空或0不分页查所有)", dataType = "Integer")
    private Integer rows;
    @ApiModelProperty(notes = "当前页", dataType = "Integer")
    private Integer page;

    @ApiModelProperty(hidden = true)
    private Integer startRow;

    @JsonIgnore
    public Integer getStartRow() {
        if (this.rows != null && this.rows > 0) {
            return (this.page != null && this.page > 0 ? this.page - 1 : 0) * this.rows;
        }
        return null;
    }
}
