package com.dfire.common.entity.vo;

import com.dfire.common.enums.JobRunTypeEnum;
import com.dfire.common.enums.JobScheduleTypeEnum;
import com.dfire.common.processor.Processor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午11:00 2018/5/16
 * @desc
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeraActionVo {

    private String id;

    private Integer jobId;

    private Boolean auto;

    private Map<String, String> configs;

    private String cronExpression;

    private String cycle;

    private List<String> dependencies;

    private List<String> jobDependencies;

    private String description;

    private Date gmtCreate;

    private Date gmtModified;

    private Integer groupId;

    private String historyId;

    private String host;

    private Date lastEndTime;

    private String lastResult;

    private String name;

    private int offset;

    private String owner;

    private List<Processor> postProcessors;

    private List<Processor> preProcessors;

    private List<String> readyDependency;

    private List<Map<String, String>> resources;

    private JobRunTypeEnum runType;

    private JobScheduleTypeEnum scheduleType;

    private String script;

    private Date startTime;

    private Long startTimestamp;

    private Date statisticStartTime;

    private Date statisticEndTime;

    private String status;

    private String timezone;

    private int hostGroupId;



}
