package com.dfire.common.entity.vo;

import com.dfire.common.constant.JobRunType;
import com.dfire.common.constant.JobScheduleType;
import com.dfire.common.constant.Status;
import com.dfire.common.processor.Processor;
import lombok.Builder;
import lombok.Data;

import java.util.*;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午11:38 2018/4/23
 * @desc
 */
@Builder
@Data
public class HeraJobVo {

    private int id;

    private String auto;

    private Map<String, String> configs;

    private String cronExpression;

    private String cycle;

    private List<String> dependencies;

    private String description;

    private Date gmtCreate;

    private Date gmtModified;

    private Integer groupId;

    private String historyId;

    private String host;

    private Date lastEndTime;

    private String lastResult;

    private String name;

    private String offset;

    private String owner;

    private List<String> postProcessors;

    private List<String> preProcessors;

    private String readyDependency;

    private Map<String, String> resources;

    private JobRunType runType;

    private JobScheduleType scheduleType;

    private String script;

    private Date startTime;

    private Long startTimestamp;

    private Date statisticEndTime;

    private Date statisticStartTime;

    private Status status;

    private String timezone;

    private String hostGroupId;

    private Long mustEndMinute;

}
