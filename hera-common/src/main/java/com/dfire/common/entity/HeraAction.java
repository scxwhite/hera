package com.dfire.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 17:31 2018/1/11
 * @desc
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HeraAction {

    private String id;

    private String jobId;

    private Integer auto = 0;

    private Integer scheduleType;

    private String runType;

    private String configs;

    private String cronExpression;

    private String dependencies;

    private String jobDependencies;

    private String name;

    private String description;

    private Integer groupId;

    private String owner;

    private String resources;

    private Date gmtCreate = new Date();

    private Date gmtModified = new Date();

    private Long historyId;

    private String status;

    private String readyDependency;

    private String preProcessors;

    private String postProcessors;

    private String timezone;

    private Date startTime;

    private long startTimestamp;

    private int offset;

    private Date lastEndTime;

    private String lastResult;

    private Date statisticStartTime;

    private Date statisticEndTime;

    private String cycle;

    private String host;

    private Integer hostGroupId;

}
