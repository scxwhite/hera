package com.dfire.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 13:59 2017/12/30
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HeraJob {

    private int id;

    private Integer auto;

    private String configs;

    private String cronExpression;

    private String cycle;

    private String dependencies;

    private String description;

    private Date gmtCreate;

    private Date gmtModified;

    private Integer groupId;

    private String historyId;

    private String host;

    private Date lastEndTime;

    private String lastResult;

    private String name;

    private Integer offset;

    private String owner;

    private String postProcessors;

    private String preProcessors;

    private String readyDependency;

    private String resources;

    private String runType;

    private Integer scheduleType;

    private String script;

    private Date startTime;

    private Long startTimestamp;

    private Date statisticEndTime;

    private Date statisticStartTime;

    private String status;

    private String timezone;

    private int hostGroupId;

    private String areaId;

    private int mustEndMinute;

    private short repeatRun;

    private Integer isValid;

}
