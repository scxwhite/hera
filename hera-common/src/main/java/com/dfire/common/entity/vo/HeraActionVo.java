package com.dfire.common.entity.vo;

import java.util.Date;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午11:00 2018/5/16
 * @desc
 */
public class HeraActionVo {

    private String id;

    private String jobId;

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

    private int offset;

    private String owner;

    private String postProcessors;

    private String preProcessors;

    private String readyDependency;

    private Map<String, String> resources;

    private String runType;

    private Integer scheduleType;

    private String script;

    private Date startTime;

    private long startTimestamp;

    private Date statisticStartTime;

    private Date statisticEndTime;

    private String status;

    private String timezone;

    private Integer hostGroupId;
}
