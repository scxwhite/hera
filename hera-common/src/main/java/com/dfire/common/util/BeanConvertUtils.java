package com.dfire.common.util;

import com.dfire.common.entity.*;
import com.dfire.common.entity.vo.*;
import com.dfire.common.enums.JobRunTypeEnum;
import com.dfire.common.enums.JobScheduleTypeEnum;
import com.dfire.common.enums.StatusEnum;
import com.dfire.common.enums.TriggerTypeEnum;
import com.dfire.common.kv.Tuple;
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

    public static HeraJobHistoryVo convert(HeraJobHistory heraJobHistory) {
        HeraJobHistoryVo heraJobHistoryVo = HeraJobHistoryVo.builder().build();
        BeanUtils.copyProperties(heraJobHistory, heraJobHistoryVo);
        if (StringUtils.isBlank(heraJobHistory.getLog())) {
            heraJobHistoryVo.setLog(LogContent.builder().build());
        } else {
            heraJobHistoryVo.setLog(LogContent.builder().content(new StringBuffer(heraJobHistory.getLog())).build());
        }
        heraJobHistoryVo.setProperties(StringUtil.convertStringToMap(heraJobHistory.getProperties()));
        heraJobHistoryVo.setStatusEnum(StatusEnum.parse(heraJobHistory.getStatus()));
        heraJobHistoryVo.setTriggerType(TriggerTypeEnum.parser(heraJobHistory.getTriggerType()));
        return heraJobHistoryVo;

    }

    public static HeraJobHistory convert(HeraJobHistoryVo jobHistoryVo) {
        HeraJobHistory jobHistory = HeraJobHistory.builder().build();
        BeanUtils.copyProperties(jobHistoryVo, jobHistory);
        jobHistory.setLog(jobHistoryVo.getLog().getContent());
        jobHistory.setStatus(jobHistoryVo.getStatusEnum().toString());
        jobHistory.setProperties(StringUtil.convertMapToString(jobHistoryVo.getProperties()));
        jobHistory.setTriggerType(jobHistoryVo.getTriggerType().getId());
        return jobHistory;
    }

    public static HeraDebugHistoryVo convert(HeraDebugHistory heraDebugHistory) {
        HeraDebugHistoryVo heraJobHistoryVo = HeraDebugHistoryVo.builder().build();
        BeanUtils.copyProperties(heraDebugHistory, heraJobHistoryVo);
        heraJobHistoryVo.setStatus(StatusEnum.parse(heraDebugHistory.getStatus()));
        heraJobHistoryVo.setRunType(JobRunTypeEnum.parser(heraDebugHistory.getRunType()));
        if (StringUtils.isBlank(heraDebugHistory.getLog())) {
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
        heraJobVo.setAuto(heraJob.getAuto() == 1 ? "开启" : "关闭");
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
        heraAction.setResources(StringUtil.convertResourceToString(heraJobVo.getResources()));
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

        jobStatus.setActionId(action.getId());
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
        Integer auto = 1;
        HeraActionVo heraActionVo = HeraActionVo.builder().build();
        BeanUtils.copyProperties(action, heraActionVo);

        heraActionVo.setPostProcessors(StringUtil.convertProcessorToList(action.getPostProcessors()));
        heraActionVo.setPreProcessors(StringUtil.convertProcessorToList(action.getPreProcessors()));
        heraActionVo.setResources(StringUtil.convertResources(action.getResources()));
        heraActionVo.setConfigs(StringUtil.convertStringToMap(action.getConfigs()));
        heraActionVo.setRunType(JobRunTypeEnum.parser(action.getRunType()));
        heraActionVo.setScheduleType(JobScheduleTypeEnum.parser(action.getScheduleType()));
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
        heraAction.setStatus(jobStatus.getStatus().toString());
        heraAction.setHistoryId(jobStatus.getHistoryId());
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
        heraJob.setId(Integer.parseInt(heraJobVo.getId()));
        return heraJob;
    }

    public static HeraGroupVo convert(HeraGroup heraGroup) {
        HeraGroupVo heraGroupVo = HeraGroupVo.builder().build();
        if (heraGroup == null) {
            return heraGroupVo;
        }
        BeanUtils.copyProperties(heraGroup, heraGroupVo);
        heraGroupVo.setConfigs(new HashMap<>(1));
        heraGroupVo.setConfigs(StringUtil.convertStringToMap(heraGroup.getConfigs()));
        heraGroupVo.setResources(new ArrayList<>());
        if(heraGroup.getResources() != null) {
            heraGroupVo.setResources(StringUtil.convertResources(heraGroup.getResources()));
        }
        heraGroupVo.setExisted(heraGroup.getExisted() == 1 ? true : false);
        return heraGroupVo;
    }

    public static HeraGroup convert(HeraGroupVo groupVo) {
        HeraGroup heraGroup = new HeraGroup();
        BeanUtils.copyProperties(groupVo, heraGroup);
        heraGroup.setExisted(groupVo.isExisted() == true ? 1 : 0);
        heraGroup.setConfigs(StringUtil.convertMapToString(groupVo.getConfigs()));
        heraGroup.setResources(StringUtil.convertResourceToString(groupVo.getResources()));

        return heraGroup;
    }

    public static void stringToMap(String str, Map<String, String> configs) {
        str = str.trim();
        String[] split = str.split("\\s");
        Arrays.stream(split).forEach(x -> {
            String[] pair = x.split("=");
            if (pair.length == 2) {
                configs.put(pair[0], pair[1]);
            }
        });
    }

}
