package com.lhiot.healthygood.feign.model;

import com.leon.microx.predefine.OnOff;
import com.lhiot.healthygood.feign.type.AdvertiseType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author xiaojian  created in  2018/11/21 10:11
 */
@Data
@ApiModel
public class Advertisement {
    @ApiModelProperty(notes = "主键Id", dataType = "Long", readOnly = true)
    private Long id;
    @ApiModelProperty(notes = "位置Id", dataType = "Long")
    private Long positionId;
    @ApiModelProperty(notes = "广告图片", dataType = "String")
    private String image;
    @NotNull(message = "广告名不能为空")
    @ApiModelProperty(notes = "广告名", dataType = "String")
    private String advertiseName;
    @ApiModelProperty(notes = "广告关联", dataType = "String")
    private String advertiseRelation;
    @ApiModelProperty(notes = "广告类别（PRODUCT_DETAILS- 商品详情 STORE_LIVE_TELECAST- 门店直播 MORE_AMUSEMENT- 多娱\n" +
            "EXTERNAL_LINKS- 外部链接）", dataType = "AdvertiseType")
    private AdvertiseType advertiseType;
    @ApiModelProperty(notes = "广告状态（ON- 开启 OFF-关闭）", dataType = "AdvertiseStatus")
    private OnOff advertiseStatus;
    @ApiModelProperty(notes = "广告序号", dataType = "Integer")
    private Integer sorting;
    @ApiModelProperty(notes = "开启时间", dataType = "Date")
    private Date beginAt;
    @ApiModelProperty(notes = "关闭时间", dataType = "Date")
    private Date endAt;
    @ApiModelProperty(notes = "创建时间", dataType = "Date", readOnly = true)
    private Date createAt;


}
