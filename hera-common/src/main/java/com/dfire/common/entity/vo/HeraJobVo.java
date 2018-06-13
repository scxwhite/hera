package com.dfire.common.entity.vo;

import com.dfire.common.enums.JobRunType;
import com.dfire.common.enums.JobScheduleType;
import com.dfire.common.enums.Status;
import com.dfire.common.processor.Processor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午11:38 2018/4/23
 * @desc
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeraJobVo {

    private String id;

    private String auto;

    private Map<String, String> configs;

    private String cronExpression;

    private String cycle;

    private String dependencies;

    private String description;

    private Date gmtCreate;

    private Date gmtModified;

    private int groupId;

    private String historyId;

    private String host;

    private Date lastEndTime;

    private String lastResult;

    private String name;

    private String offset;

    private String owner;

    private List<Processor> postProcessors;

    private List<Processor> preProcessors;

    private String readyDependency;

    private List<Map<String, String>> resources;

    private JobRunType runType;

    private String scheduleType;

    private String script;

    private Date startTime;

    private Long startTimestamp;

    private Date statisticEndTime;

    private Date statisticStartTime;

    private String status;

    private String timezone;

    private int hostGroupId;

    private Long mustEndMinute;

    private String rollBackTimes;

    private String rollBackWaitTime;

    private String runPriorityLevel;

    private String selfConfigs;

    private String resource;

}
