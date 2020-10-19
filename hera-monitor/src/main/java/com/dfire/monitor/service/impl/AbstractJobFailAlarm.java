package com.dfire.monitor.service.impl;

import com.dfire.common.constants.Constants;
import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.HeraSso;
import com.dfire.common.service.JobFailAlarm;
import com.dfire.common.util.ActionUtil;
import com.dfire.common.vo.JobElement;
import com.dfire.config.HeraGlobalEnv;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * desc:
 *
 * @author scx
 * @create 2019/04/28
 */
public abstract class AbstractJobFailAlarm implements JobFailAlarm {


    /**
     * 任务失败告警
     * @param heraJob       任务实例
     * @param runCount      运行次数
     * @param monitorUser   监控人
     * @return              消息
     */
    protected String buildJobErrorMsg(HeraJob heraJob, int runCount, Set<HeraSso> monitorUser) {

        return "hera任务失败了 \n"
                + "环境:" + HeraGlobalEnv.getEnv() + "\n"
                + "区域:" + HeraGlobalEnv.getArea() + "\n"
                + "名称:" + heraJob.getName() + "\n"
                + "描述:" + heraJob.getDescription() + "\n"
                + "任务ID:" + heraJob.getId() + "\n"
                + "失败次数:" + runCount + "\n"
                + "负责人:" + monitorUser.stream().map(HeraSso::getName).collect(Collectors.joining(Constants.SEMICOLON)) + "\n";
    }


    protected String buildTimeoutMsg(JobElement element, Set<HeraSso> monitorUser) {
        return "【警告】任务执行超时\n"
                + "环境:" + HeraGlobalEnv.getEnv() + "\n"
                + "区域:" + HeraGlobalEnv.getArea() + "\n"
                + "任务ID:" + ActionUtil.getJobId(element.getJobId()) + "\n"
                + "预计时长:" + element.getCostMinute() + "分钟"
                + "负责人:" + monitorUser.stream().map(HeraSso::getName).collect(Collectors.joining(Constants.SEMICOLON)) + "\n";
    }
}
