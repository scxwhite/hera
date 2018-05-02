package com.dfire.common.entity.vo;

import com.dfire.common.constant.JobRunType;
import com.dfire.common.constant.JobScheduleType;
import com.dfire.common.processor.Processor;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午11:38 2018/4/23
 * @desc
 */
@Builder
@Data
public class HeraJobVo {

    private Map<String, String> properties = new HashMap<String, String>();
    private String cronExpression;
    private List<String> dependencies = new ArrayList<String>();
    private String id;
    private String toJobId;
    private String name;
    private String desc;
    private String groupId;
    private String owner;
    private Boolean auto = false;
    private List<Map<String, String>> resources = new ArrayList<Map<String, String>>();
    private JobRunType jobRunType;
    private JobScheduleType jobScheduleType;
    private String timezone;
    private List<Processor> preProcessors = new ArrayList<Processor>();
    private List<Processor> postProcessors = new ArrayList<Processor>();
    private String offRaw = "0";
    private String cycle;
    private long startTimestamp;
    private String startTime;
    private String statisticStartTime;
    private String statisticEndTime;
    private String host;
    private String hostGroupId;
}
