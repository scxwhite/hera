package com.dfire.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 17:31 2018/1/11
 * @desc
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ZeusAction {

    private static final long serialVersionUID = 1L;

    private Map<String, String> allProperties = new HashMap<String, String>();
    private Map<String, String> localProperties = new HashMap<String, String>();
    private String cronExpression;
    private List<String> dependencies;
    private String id;
    private String toJobId;
    private String name;
    private String desc;
    private String groupId;
    private String owner;
    private List<String> owners;
    private String ownerName;
    private List<Map<String, String>> localResources = new ArrayList<Map<String, String>>();
    private List<Map<String, String>> allResources = new ArrayList<Map<String, String>>();
    private String jobRunType;
    private String jobScheduleType;
    private Boolean auto;
    private String script;
    private List<String> preProcessers = new ArrayList<String>();
    private List<String> postProcessers = new ArrayList<String>();
    private Boolean admin;
    private List<String> admins = new ArrayList<String>();
    private List<String> follows = new ArrayList<String>();
    private Map<String, String> readyDependencies = new HashMap<String, String>();
    private String status;
    private String lastStatus;
    private String historyId;
    private String defaultTZ;
    private String offRaw;
    private String jobCycle;
    private String host;

    public static final String MapReduce = "MapReduce程序";
    public static final String SHELL = "shell 脚本";
    public static final String HIVE = "hive 脚本";

    public static final String TIMING_JOB = "定时调度";
    public static final String DEPEND_JOB = "依赖调度";
    public static final String CYCLE_JOB = "周期调度";

    public static final String JOB_CYCLE_HOUR="小时任务";
    public static final String JOB_CYCLE_DAY="天任务";

}
