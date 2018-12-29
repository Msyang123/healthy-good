package com.lhiot.healthygood.feign.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.leon.microx.predefine.OnOff;
import com.lhiot.healthygood.feign.type.AdvertiseType;
import com.lhiot.healthygood.feign.type.RelationType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author xiaojian  created in  2018/11/21 12:21
 */
@ApiModel
@Data
public class AdvertisementParam {
    @ApiModelProperty(notes = "位置Id", dataType = "Long")
    private Long positionId;
    @ApiModelProperty(notes = "广告名", dataType = "String")
    private String advertiseName;
    @ApiModelProperty(notes = "广告类别（IMAGE- 图片广告 TEXT -文字广告）", dataType = "AdvertiseType")
    private AdvertiseType advertiseType;
    @ApiModelProperty(notes = "广告关联类别（PRODUCT_DETAILS- 商品详情 STORE_LIVE_TELECAST- 门店直播 MORE_AMUSEMENT- 多娱\n" +
            "EXTERNAL_LINKS- 外部链接）", dataType = "RelationType")
    private RelationType relationType;
    @ApiModelProperty(notes = "广告状态（ON- 开启 OFF-关闭）", dataType = "AdvertiseStatus")
    private OnOff advertiseStatus;
    @ApiModelProperty(notes = "起始创建时间", dataType = "Date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date beginCreateAt;
    @ApiModelProperty(notes = "截止创建时间", dataType = "Date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
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
