package com.dfire.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 17:31 2018/1/11
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HeraAction {

    private Long id;

    private Integer jobId;

    private Integer auto = 0;

    private String configs;

    private String cronExpression;

    private String cycle;

    private String dependencies;

    private String jobDependencies;

    private String description;

    private Date gmtCreate;

    private Date gmtModified;


    private Integer groupId;

    private Long historyId;

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

    private Date statisticStartTime;

    private Date statisticEndTime;

    private String status;

    private String timezone;

    private int hostGroupId;

    private String batchId;



}
