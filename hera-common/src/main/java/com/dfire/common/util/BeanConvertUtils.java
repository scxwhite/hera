package com.dfire.common.util;

import com.dfire.common.constants.Constants;
import com.dfire.common.entity.*;
import com.dfire.common.entity.vo.*;
import com.dfire.common.enums.JobRunTypeEnum;
import com.dfire.common.enums.JobScheduleTypeEnum;
import com.dfire.common.enums.StatusEnum;
import com.dfire.common.enums.TriggerTypeEnum;
import com.dfire.common.kv.Tuple;
import com.dfire.common.vo.JobStatus;
import com.dfire.common.vo.LogContent;
import com.dfire.logs.ErrorLog;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午10:52 2018/5/2
 * @desc
 */
public class BeanConvertUtils {

    public static HeraProfileVo convert(HeraProfile heraProfile) {
        HeraProfileVo heraProfileVo = HeraProfileVo.builder().build();
        if (heraProfile.getHadoopConf() != null) {
            heraProfileVo.setHadoopConf(StringUtil.convertStringToMap(heraProfile.getHadoopConf()));
        }
        BeanUtils.copyProperties(heraProfile, heraProfileVo);
        return heraProfileVo;
    }

    public static HeraProfile convert(HeraProfileVo heraProfileVo) {
        HeraProfile heraProfile = HeraProfile.builder().build();
        if (heraProfileVo.getHadoopConf() != null) {
            heraProfile.setHadoopConf(StringUtil.convertMapToString(heraProfileVo.getHadoopConf()));
        }
        BeanUtils.copyProperties(heraProfile, heraProfileVo);
        return heraProfile;
    }

    public static HeraJobHistoryVo convert(HeraJobHistory heraJobHistory) {
        HeraJobHistoryVo heraJobHistoryVo = HeraJobHistoryVo.builder().build();
        BeanUtils.copyProperties(heraJobHistory, heraJobHistoryVo);
        if (StringUtils.isBlank(heraJobHistory.getLog())) {
            heraJobHistoryVo.setLog(new LogContent());
        } else {
            heraJobHistoryVo.setLog(new LogContent(new StringBuffer(heraJobHistory.getLog())));
        }
        heraJobHistoryVo.setProperties(StringUtil.convertStringToMap(heraJobHistory.getProperties()));
        heraJobHistoryVo.setStatusEnum(StatusEnum.parse(heraJobHistory.getStatus()));
        heraJobHistoryVo.setTriggerType(TriggerTypeEnum.parser(heraJobHistory.getTriggerType()));
        heraJobHistoryVo.setHostGroupId(heraJobHistory.getHostGroupId());
        return heraJobHistoryVo;

    }

    public static HeraJobHistory convert(HeraJobHistoryVo jobHistoryVo) {
        HeraJobHistory jobHistory = HeraJobHistory.builder().build();
        BeanUtils.copyProperties(jobHistoryVo, jobHistory);
        jobHistory.setLog(jobHistoryVo.getLog().getContent());
        jobHistory.setStatus(jobHistoryVo.getStatusEnum() == null ? null : jobHistoryVo.getStatusEnum().toString());
        jobHistory.setProperties(StringUtil.convertMapToString(jobHistoryVo.getProperties()));
        jobHistory.setTriggerType(jobHistoryVo.getTriggerType().getId());

        return jobHistory;
    }

    public static HeraDebugHistoryVo convert(HeraDebugHistory heraDebugHistory) {
        HeraDebugHistoryVo heraJobHistoryVo = HeraDebugHistoryVo.builder().build();
        BeanUtils.copyProperties(heraDebugHistory, heraJobHistoryVo);
        heraJobHistoryVo.setStatus(StatusEnum.parse(heraDebugHistory.getStatus()));
        heraJobHistoryVo.setRunType(JobRunTypeEnum.parser(heraDebugHistory.getRunType()));
        heraJobHistoryVo.setJobId(heraDebugHistory.getJobId());

        if (heraDebugHistory.getStartTime() != null) {
            heraJobHistoryVo.setStartTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(heraDebugHistory.getStartTime()));
        }
        if (heraDebugHistory.getEndTime() != null) {
            heraJobHistoryVo.setEndTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(heraDebugHistory.getEndTime()));
        }
        if (heraDebugHistory.getGmtCreate() != null) {
            heraJobHistoryVo.setGmtCreate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(heraDebugHistory.getGmtCreate()));
        }
        if (heraDebugHistory.getGmtModified() != null) {
            heraJobHistoryVo.setGmtModified(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(heraDebugHistory.getGmtModified()));
        }
        if (StringUtils.isBlank(heraDebugHistory.getLog())) {
            heraJobHistoryVo.setLog(new LogContent());
        } else {
            heraJobHistoryVo.setLog(new LogContent(new StringBuffer(heraDebugHistory.getLog())));
        }
        return heraJobHistoryVo;

    }

    public static HeraDebugHistory convert(HeraDebugHistoryVo jobHistoryVo) {
        HeraDebugHistory jobHistory = HeraDebugHistory.builder().build();
        BeanUtils.copyProperties(jobHistoryVo, jobHistory);
        jobHistory.setStatus(jobHistoryVo.getStatus().toString());
        jobHistory.setLog(jobHistoryVo.getLog().getContent());
        jobHistory.setRunType(jobHistoryVo.getRunType().toString());
        try {
            if (jobHistoryVo.getStartTime() != null) {
                jobHistory.setStartTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(jobHistoryVo.getStartTime()));
            }
            if (jobHistoryVo.getEndTime() != null) {
                jobHistory.setEndTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(jobHistoryVo.getEndTime()));
            }
            if (jobHistoryVo.getGmtCreate() != null) {
                jobHistory.setGmtCreate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(jobHistoryVo.getGmtCreate()));
            }
            if (jobHistoryVo.getGmtModified() != null) {
                jobHistory.setGmtModified(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(jobHistoryVo.getGmtModified()));
            }
        } catch (Exception e) {
            ErrorLog.error("解析日期异常", e);
        }
        return jobHistory;
    }

    public static HeraJobVo convert(HeraJob heraJob) {
        HeraJobVo heraJobVo = HeraJobVo.builder().build();
        BeanUtils.copyProperties(heraJob, heraJobVo);
        heraJobVo.setConfigs(StringUtil.convertStringToMap(heraJob.getConfigs()));
        heraJobVo.setPostProcessors(StringUtil.convertProcessorToList(heraJob.getPostProcessors()));
        heraJobVo.setPreProcessors(StringUtil.convertProcessorToList(heraJob.getPreProcessors()));
        heraJobVo.setResources(StringUtil.convertResources(heraJob.getResources()));
        heraJobVo.setAuto(heraJob.getAuto() == 1 ? Constants.OPEN_STATUS : heraJob.getAuto() == 0 ? Constants.CLOSE_STATUS : Constants.INVALID_STATUS);
        heraJobVo.setDependencies(heraJob.getDependencies());
        heraJobVo.setRunType(JobRunTypeEnum.parser(heraJob.getRunType()));
        heraJobVo.setEstimatedEndHour(ActionUtil.intTOHour(heraJob.getEstimatedEndHour()));
        return heraJobVo;
    }

    /**
     * job -> jobAction
     *
     * @param heraJobVo
     * @return
     */
    public static HeraAction convert(HeraJobVo heraJobVo) {
        HeraAction heraAction = HeraAction.builder().build();
        BeanUtils.copyProperties(heraJobVo, heraAction);
        heraAction.setPostProcessors(StringUtil.convertProcessorToList(heraJobVo.getPostProcessors()));
        heraAction.setPreProcessors(StringUtil.convertProcessorToList(heraJobVo.getPreProcessors()));
        heraAction.setResources(StringUtil.convertResourceToString(heraJobVo.getResources()));
        heraAction.setConfigs(StringUtil.convertMapToString(heraJobVo.getConfigs()));
        return heraAction;
    }


    /**
     * job中jobVo为对应的版本id
     *
     * @param job
     * @return
     */
    public static HeraAction convert(Tuple<HeraJobVo, JobStatus> job) {
        if (job == null) {
            return null;
        }
        HeraAction heraAction = convert(job.getSource());
        HeraAction action = convert(job.getTarget());
        heraAction.setReadyDependency(action.getReadyDependency());
        heraAction.setStatus(action.getStatus());
        heraAction.setHistoryId(action.getHistoryId());
        return heraAction;
    }

    public static Tuple<HeraActionVo, JobStatus> convert(HeraAction action) {
        HeraActionVo heraActionVo = transform(action);
        JobStatus jobStatus = JobStatus.builder().build();
        jobStatus.setActionId(action.getId());
        jobStatus.setHistoryId(action.getHistoryId());
        jobStatus.setStatus(StatusEnum.parse(action.getStatus()));
        jobStatus.setReadyDependency(StringUtil.convertStringToMap(action.getReadyDependency()));
        return new Tuple<>(heraActionVo, jobStatus);

    }

    /**
     * action 版本转为job
     *
     * @param action
     * @return
     */
    public static HeraActionVo transform(HeraAction action) {
        Integer auto = 1;
        HeraActionVo heraActionVo = HeraActionVo.builder().build();
        BeanUtils.copyProperties(action, heraActionVo);
        if (action.getDependencies() != null && !StringUtils.isBlank(action.getDependencies())) {
            heraActionVo.setDependencies(Arrays.stream(action.getDependencies().split(Constants.COMMA)).map(Long::parseLong).collect(Collectors.toList()));
        }
        if (action.getJobDependencies() != null && !StringUtils.isBlank(action.getJobDependencies())) {
            heraActionVo.setJobDependencies(Arrays.stream(action.getJobDependencies().split(Constants.COMMA)).map(Long::parseLong).collect(Collectors.toList()));
        }
        heraActionVo.setPostProcessors(StringUtil.convertProcessorToList(action.getPostProcessors()));
        heraActionVo.setPreProcessors(StringUtil.convertProcessorToList(action.getPreProcessors()));
        heraActionVo.setResources(StringUtil.convertResources(action.getResources()));
        try {
            heraActionVo.setConfigs(StringUtil.convertStringToMap(action.getConfigs()));
        } catch (RuntimeException e) {
            ErrorLog.error("json parse error on " + action.getId(), e);
        }
        heraActionVo.setRunType(JobRunTypeEnum.parser(action.getRunType()));
        heraActionVo.setScheduleType(JobScheduleTypeEnum.parser(action.getScheduleType()));
        heraActionVo.setId(action.getId());
        if (action.getAuto().equals(auto)) {
            heraActionVo.setAuto(true);
        } else {
            heraActionVo.setAuto(false);
        }
        return heraActionVo;
    }

    public static HeraAction convert(JobStatus jobStatus) {
        HeraAction heraAction = HeraAction.builder().build();

        if (jobStatus == null) {
            return heraAction;
        }

        heraAction.setId(jobStatus.getActionId());
        heraAction.setStatus(jobStatus.getStatus() == null ? null : jobStatus.getStatus().toString());
        heraAction.setHistoryId(jobStatus.getHistoryId());
        heraAction.setStartTime(jobStatus.getStartTime());
        heraAction.setLastEndTime(jobStatus.getEndTime());
        heraAction.setReadyDependency(StringUtil.convertMapToString(jobStatus.getReadyDependency()));
        return heraAction;
    }


    public static HeraJob convertToHeraJob(HeraJobVo heraJobVo) {
        HeraJob heraJob = new HeraJob();
        BeanUtils.copyProperties(heraJobVo, heraJob);
        Map<String, String> configs = new HashMap<>(64);
        configs.put("roll.back.times", heraJobVo.getRollBackTimes());
        configs.put("roll.back.wait.time", heraJobVo.getRollBackWaitTime());
        configs.put("run.priority.level", heraJobVo.getRunPriorityLevel());
        Optional<String> selfConfigs = Optional.ofNullable(heraJobVo.getSelfConfigs());
        selfConfigs.ifPresent(s -> {
            stringToMap(s, configs);
            heraJob.setConfigs(StringUtil.convertMapToString(configs));
        });
        heraJob.setEstimatedEndHour(ActionUtil.hourToInt(heraJobVo.getEstimatedEndHour()));
        heraJob.setRunType(heraJobVo.getRunType().toString());
        return heraJob;
    }

    public static HeraGroupVo convert(HeraGroup heraGroup) {
        HeraGroupVo heraGroupVo = HeraGroupVo.builder().build();
        if (heraGroup == null) {
            return heraGroupVo;
        }
        BeanUtils.copyProperties(heraGroup, heraGroupVo);
        heraGroupVo.setConfigs(StringUtil.convertStringToMap(heraGroup.getConfigs()));
        heraGroupVo.setResources(new ArrayList<>());
        if (heraGroup.getResources() != null) {
            heraGroupVo.setResources(StringUtil.convertResources(heraGroup.getResources()));
        }
        heraGroupVo.setExisted(heraGroup.getExisted() == 1);
        return heraGroupVo;
    }

    public static HeraGroup convert(HeraGroupVo groupVo) {
        HeraGroup heraGroup = new HeraGroup();
        BeanUtils.copyProperties(groupVo, heraGroup);
        Map<String, String> configs = new HashMap<>();
        Optional<String> selfConfigs = Optional.ofNullable(groupVo.getSelfConfigs());
        selfConfigs.ifPresent(s -> {
            stringToMap(s, configs);
            heraGroup.setConfigs(StringUtil.convertMapToString(configs));
        });
        heraGroup.setResources(StringUtil.convertResourceToString(groupVo.getResources()));

        return heraGroup;
    }

    public static void stringToMap(String str, Map<String, String> configs) {
        str = str.trim();
        String[] split = str.split("\n");
        Arrays.stream(split).forEach(x -> {
            int index = x.indexOf("=");
            if (index != -1) {
                configs.put(x.substring(0, index).trim(), x.substring(index + 1, x.length()).trim());
            }
        });
    }

}
