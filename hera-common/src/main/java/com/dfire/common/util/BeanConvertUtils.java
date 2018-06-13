package com.dfire.common.util;

import com.dfire.common.entity.*;
import com.dfire.common.entity.vo.*;
import com.dfire.common.enums.JobRunType;
import com.dfire.common.enums.JobScheduleType;
import com.dfire.common.enums.Status;
import com.dfire.common.enums.TriggerType;
import com.dfire.common.kv.Tuple;
import com.dfire.common.processor.Processor;
import com.dfire.common.vo.JobStatus;
import com.dfire.common.vo.LogContent;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.*;

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

    public static HeraGroupVo convert(HeraGroup heraGroup) {
        HeraGroupVo heraGroupVo = HeraGroupVo.builder().build();
        BeanUtils.copyProperties(heraGroup, heraGroupVo);
        return heraGroupVo;
    }

    public static HeraJobHistoryVo convert(HeraJobHistory heraJobHistory) {
        HeraJobHistoryVo heraJobHistoryVo = HeraJobHistoryVo.builder().build();
        BeanUtils.copyProperties(heraJobHistory, heraJobHistoryVo);
        if(StringUtils.isBlank(heraJobHistory.getLog())) {
            heraJobHistoryVo.setLog(LogContent.builder().build());
        } else {
            heraJobHistoryVo.setLog(LogContent.builder().content(new StringBuffer(heraJobHistory.getLog())).build());
        }
        heraJobHistoryVo.setProperties(StringUtil.convertStringToMap(heraJobHistory.getProperties()));
        heraJobHistoryVo.setStatus(Status.parse(heraJobHistory.getStatus()));
        heraJobHistoryVo.setTriggerType(TriggerType.parser(heraJobHistory.getTriggerType()));
        return heraJobHistoryVo;

    }

    public static HeraJobHistory convert(HeraJobHistoryVo jobHistoryVo) {
        HeraJobHistory jobHistory = HeraJobHistory.builder().build();
        BeanUtils.copyProperties(jobHistoryVo, jobHistory);
        jobHistory.setLog(jobHistoryVo.getLog().getContent());
        jobHistory.setStatus(jobHistoryVo.getStatus().toString());
        jobHistory.setProperties(StringUtil.convertMapToString(jobHistoryVo.getProperties()));
        jobHistory.setTriggerType(jobHistoryVo.getTriggerType().getId());
        return jobHistory;
    }

    public static HeraDebugHistoryVo convert(HeraDebugHistory heraDebugHistory) {
        HeraDebugHistoryVo heraJobHistoryVo = HeraDebugHistoryVo.builder().build();
        BeanUtils.copyProperties(heraDebugHistory, heraJobHistoryVo);
        heraJobHistoryVo.setStatus(Status.parse(heraDebugHistory.getStatus()));
        heraJobHistoryVo.setRunType(JobRunType.parser(heraDebugHistory.getRunType()));
        if(StringUtils.isBlank(heraDebugHistory.getLog())) {
            heraJobHistoryVo.setLog(LogContent.builder().build());
        } else {
            heraJobHistoryVo.setLog(LogContent.builder().content(new StringBuffer(heraDebugHistory.getLog())).build());
        }
        return heraJobHistoryVo;

    }

    public static HeraDebugHistory convert(HeraDebugHistoryVo jobHistoryVo) {
        HeraDebugHistory jobHistory = HeraDebugHistory.builder().build();
        BeanUtils.copyProperties(jobHistoryVo, jobHistory);
        jobHistory.setStatus(jobHistoryVo.getStatus().toString());
        jobHistory.setLog(jobHistoryVo.getLog().getContent());
        jobHistory.setRunType(jobHistoryVo.getRunType().toString());
        return jobHistory;
    }

    public static HeraJobVo convert(HeraJob heraJob) {
        HeraJobVo heraJobVo = HeraJobVo.builder().build();
        BeanUtils.copyProperties(heraJob, heraJobVo);
        heraJobVo.setConfigs(StringUtil.convertStringToMap(heraJob.getConfigs()));
        heraJobVo.setPostProcessors(StringUtil.convertProcessorToList(heraJob.getPostProcessors()));
        heraJobVo.setPreProcessors(StringUtil.convertProcessorToList(heraJob.getPreProcessors()));
        heraJobVo.setResources(StringUtil.convertResources(heraJob.getResources()));
        heraJobVo.setId(String.valueOf(heraJob.getId()));
        heraJobVo.setAuto(Objects.equals("1", heraJob.getAuto()) ? "开启" : "关闭");
        heraJobVo.setDependencies(heraJob.getDependencies());

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
        heraAction.setResources(StringUtil.convertResoureToString(heraJobVo.getResources()));
        heraAction.setConfigs(StringUtil.convertMapToString(heraJobVo.getConfigs()));
        heraAction.setJobId(heraJobVo.getId());
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
        jobStatus.setJobId(action.getJobId());
        jobStatus.setHistoryId(action.getHistoryId());
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
        HeraActionVo heraActionVo = HeraActionVo.builder().build();
        BeanUtils.copyProperties(action, heraActionVo);

        heraActionVo.setPostProcessors(StringUtil.convertProcessorToList(action.getPostProcessors()));
        heraActionVo.setPreProcessors(StringUtil.convertProcessorToList(action.getPreProcessors()));
        heraActionVo.setResources(StringUtil.convertResources(action.getResources()));
        heraActionVo.setConfigs(StringUtil.convertStringToMap(action.getConfigs()));
        heraActionVo.setRunType(JobRunType.parser(action.getRunType()));
        heraActionVo.setScheduleType(JobScheduleType.parser(action.getScheduleType()));
        return heraActionVo;
    }

    public static HeraAction convert(JobStatus jobStatus) {
        if (jobStatus == null) {
            return null;
        }
        HeraAction heraAction = HeraAction.builder().build();
        heraAction.setId(jobStatus.getJobId());
        heraAction.setStatus(jobStatus.getStatus().toString());
        heraAction.setHistoryId(jobStatus.getHistoryId());
        heraAction.setReadyDependency(StringUtil.convertMapToString(jobStatus.getReadyDependency()));
        return heraAction;
    }


    public static HeraJob convertToHeraJob(HeraJobVo heraJobVo) {
        HeraJob heraJob = new HeraJob();
        BeanUtils.copyProperties(heraJobVo, heraJob);
        Map<String, String> configs = new HashMap<>();
        configs.put("roll.back.times", heraJobVo.getRollBackTimes());
        configs.put("roll.back.wait.time", heraJobVo.getRollBackWaitTime());
        configs.put("run.priority.level", heraJobVo.getRunPriorityLevel());

        return heraJob;
    }
}
