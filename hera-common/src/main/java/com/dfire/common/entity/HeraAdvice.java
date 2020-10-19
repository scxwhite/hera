package com.dfire.common.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xiaosuda
 * @date 2018/12/5
 */
@Data
@ApiModel("建议/留言对象")
public class HeraAdvice {
    @ApiModelProperty("id")
    private Integer id;
    @ApiModelProperty("消息内容")
    private String msg;
    @ApiModelProperty("地址")
    private String address;
    @ApiModelProperty("颜色")
    private String color;
    @ApiModelProperty("创建时间")
    private String createTime;
}
