package com.dfire.monitor.service.impl;

import com.dfire.common.entity.HeraJob;
import com.dfire.common.util.ActionUtil;
import com.dfire.common.vo.JobElement;
import com.dfire.config.HeraGlobalEnv;
import com.dfire.common.service.JobFailAlarm;

/**
 * desc:
 *
 * @author scx
 * @create 2019/04/28
 */
public abstract class AbstractJobFailAlarm implements JobFailAlarm {


    protected String buildJobErrorMsg(HeraJob heraJob, int runCount) {
        return "hera任务失败了 \n"
                + "环境:" + HeraGlobalEnv.getEnv() + "\n"
                + "区域:" + HeraGlobalEnv.getArea() + "\n"
                + "名称:" + heraJob.getName() + "\n"
                + "描述:" + heraJob.getDescription() + "\n"
                + "任务ID:" + heraJob.getId() + "\n"
                + "失败次数:" + runCount + "\n";
    }


    protected String buildTimeoutMsg(JobElement element) {
        return "【警告】任务执行超时\n"
                + "环境:" + HeraGlobalEnv.getEnv() + "\n"
                + "区域:" + HeraGlobalEnv.getArea() + "\n"
                + "任务ID:" + ActionUtil.getJobId(element.getJobId()) + "\n"
                + "预计时长:" + element.getCostMinute() + "分钟";
    }
}