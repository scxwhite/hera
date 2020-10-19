package com.dfire.common.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 17:32 2018/1/11
 * @desc 开发中心脚本记录
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "任务重跑对象")

public class HeraRerun {
    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "任务id")
    private Integer jobId;
    @ApiModelProperty(value = "是否结束")
    private Integer isEnd;
    @ApiModelProperty(value = "重跑名称")
    private String name;
    @ApiModelProperty(value = "任务开始日期")
    private Long startMillis;
    @ApiModelProperty(value = "任务结束日期")
    private Long endMillis;
    @ApiModelProperty(value = "创建时间戳")
    private Long gmtCreate;
    @ApiModelProperty(value = "创建人name")
    private String ssoName;
    @ApiModelProperty(value = "其它配置")
    private String extra;
    @ApiModelProperty(value = "当前执行的版本")
    private Long actionNow;

}
