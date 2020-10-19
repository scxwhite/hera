package com.dfire.common.entity;

import com.dfire.common.config.SkipColumn;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Objects;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 13:59 2017/12/30
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "hera任务对象")
public class HeraJob {
    @ApiModelProperty(value = "组ID")
    private int id;
    @ApiModelProperty(value = "是否开启，1：开启，0：关闭")
    private Integer auto;
    @ApiModelProperty(value = "任务配置参数，json格式")
    private String configs;
    @ApiModelProperty(value = "定时表达式")
    private String cronExpression;
    @ApiModelProperty(value = "周期任务，目前只有一个：SELF_LAST")
    private String cycle;
    @ApiModelProperty(value = "依赖任务id，多个,分割")
    private String dependencies;
    @ApiModelProperty(value = "任务描述")
    private String description;
    @ApiModelProperty(value = "创建时间")
    @SkipColumn
    private Date gmtCreate;
    @ApiModelProperty(value = "更改时间")
    @SkipColumn
    private Date gmtModified;

    @ApiModelProperty(value = "组id")
    private Integer groupId;
    @ApiModelProperty(value = "废弃")
    private Long historyId;
    @ApiModelProperty(value = "废弃")
    private String host;
    @ApiModelProperty(value = "废弃")
    private Date lastEndTime;
    @ApiModelProperty(value = "废弃")
    private String lastResult;
    @ApiModelProperty(value = "任务名称")
    private String name;
    @ApiModelProperty(value = "告警级别，0：邮件，1：微信，2电话")
    private Integer offset;
    @ApiModelProperty(value = "任务管理者所在组")
    private String owner;
    @ApiModelProperty(value = "废弃")
    private String postProcessors;
    @ApiModelProperty(value = "废弃")
    private String preProcessors;
    @ApiModelProperty(value = "废弃")
    private String readyDependency;
    @ApiModelProperty(value = "废弃")
    private String resources;
    @ApiModelProperty(value = "执行类型，shell,spark,hive")
    private String runType;
    @ApiModelProperty(value = "调度类型，0：自动调度，1：依赖调度")
    private Integer scheduleType;
    @ApiModelProperty(value = "脚本内容")
    private String script;
    @ApiModelProperty(value = "废弃")
    private Date startTime;
    @ApiModelProperty(value = "废弃")
    private Long startTimestamp;
    @ApiModelProperty(value = "废弃")
    private Date statisticEndTime;
    @ApiModelProperty(value = "废弃")
    private Date statisticStartTime;
    @ApiModelProperty(value = "废弃")
    private String status;
    @ApiModelProperty(value = "废弃")
    private String timezone;
    @ApiModelProperty(value = "执行的机器组id")
    private int hostGroupId;
    @ApiModelProperty(value = "执行的区域集合，多个,分割")
    private String areaId;
    @ApiModelProperty(value = "任务的最大执行分钟数")
    private int mustEndMinute;
    @ApiModelProperty(value = "应该完成时间点，从0点开始按照分钟计时，比如要在1:20分钟完成任务，该值为80")
    private int estimatedEndHour;
    @ApiModelProperty(value = "是否允许重复执行")
    private short repeatRun;
    @ApiModelProperty(value = "0已删除，1存在")
    private Integer isValid;
    @ApiModelProperty(value = "调度周期")
    private String cronPeriod;
    @ApiModelProperty(value = "基准时间")
    private int cronInterval;
    @ApiModelProperty(value = "业务标签")
    private String bizLabel;



    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HeraJob job = (HeraJob) o;
        return id == job.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
