package com.lhiot.healthygood.domain.store;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lhiot.healthygood.entity.StorePosition;
import com.lhiot.healthygood.entity.StoreStatus;
import com.lhiot.healthygood.entity.StoreType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
* Description:门店实体类
* @author Limiaojun
* @date 2018/06/04
*/
@Data
@ApiModel
public class Store {

    /**
    *门店id
    */
    @ApiModelProperty(notes = "门店id", dataType = "Long")
    private Long id;

    /**
    *门店编码
    */
    @ApiModelProperty(notes = "门店编码", dataType = "String")
    private String code;

    /**
    *门店名称
    */
    @ApiModelProperty(notes = "门店名称", dataType = "String")
    private String name;

    /**
    *门店地址
    */
    @ApiModelProperty(notes = "门店地址", dataType = "String")
    private String address;

    /**
    *联系方式
    */
    @ApiModelProperty(notes = "联系方式", dataType = "String")
    private String phone;

    /**
    *门店图片
    */
    @ApiModelProperty(notes = "门店图片", dataType = "String")
    private String image;

    /**
    *所属区域
    */
    @ApiModelProperty(notes = "所属区域", dataType = "String")
    private String area;

    /**
    *门店状态(0-未开启  1-开启)
    */
    @ApiModelProperty(notes = "门店状态 ENABLED(\"营业\"),DISABLED(\"未营业\");", dataType = "StoreStatusEnum")
    private StoreStatus status;

    /**
    *旗舰店ID
    */
    @ApiModelProperty(notes = "旗舰店ID", dataType = "Long")
    private Long Flagship;

    /**
    *门店类型：00-普通门店  01-旗舰店
    */
    @ApiModelProperty(notes = "门店类型：ORDINARY_STORE(\"普通门店\"),FLAGSHIP_STORE (\"旗舰店\");", dataType = "StoreTypeEnum")
    private StoreType storeType;

    /**
    *门店视频
    */
    @ApiModelProperty(notes = "门店视频", dataType = "String")
    private String videoUrl;

    /**
    *直播开始时间
    */
    @ApiModelProperty(notes = "直播开始时间", dataType = "String")
    private String beginAt;

    /**
    *直播结束时间
    */
    @ApiModelProperty(notes = "直播结束时间", dataType = "String")
    private String endAt;

    /**
    *录播地址
    */
    @ApiModelProperty(notes = "录播地址", dataType = "String")
    private String tapeUrl;

    @ApiModelProperty(notes="门店位置信息",dataType="StorePosition")
    private StorePosition storePosition;

    private String distance;

    @JsonIgnore
    @ApiModelProperty(value = "当前页,默认值1")
    private Long page = 1L;

    /**
     * 传入-1可不分页
     */
    @JsonIgnore
    @ApiModelProperty(value = "每页显示条数,默认值10")
    private Long rows = 10L;

}
