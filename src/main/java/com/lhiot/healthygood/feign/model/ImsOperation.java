package com.lhiot.healthygood.feign.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
* Description:功能操作实体类
* @author yijun
* @date 2018/09/29
*/
@Data
@ToString(callSuper = true)
@ApiModel
@NoArgsConstructor
public class ImsOperation {

    /**
    *id
    */
    @JsonProperty("id")
    @ApiModelProperty(value = "id", dataType = "Long")
    private Long id;

    /**
    *菜单id
    */
    @JsonProperty("menuId")
    @ApiModelProperty(value = "菜单id", dataType = "Long")
    private Long menuId;

    /**
    *操作名称
    */
    @JsonProperty("name")
    @ApiModelProperty(value = "操作名称", dataType = "String")
    private String name;

    /**
    *拦截地址
    */
    @JsonProperty("antUrl")
    @ApiModelProperty(value = "拦截地址", dataType = "String")
    private String antUrl;

    /**
    *操作类型
    */
    @JsonProperty("type")
    @ApiModelProperty(value = "操作类型", dataType = "String")
    private String type;

    @JsonIgnore
    @JsonProperty("page")
    @ApiModelProperty(value = "当前页,默认值1")
    private Long page = 1L;

    /**
     * 传入-1可不分页
     */
    @JsonIgnore
    @JsonProperty("rows")
    @ApiModelProperty(value = "每页显示条数,默认值10")
    private Long rows = 10L;

    /**
     * 分页的起始行
     */
    @JsonIgnore
    @ApiModelProperty(value = "开始行数(执行sql时用)", hidden = true)
    private Long startRow = 0L;

    public Long getStartRow(){
        return ((rows != null && page != null) ? (page - 1) * rows : 0);
    }


}
