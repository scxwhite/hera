package com.dfire.core.netty.master;

import com.dfire.common.constants.Constants;
import com.dfire.common.entity.HeraAction;
import com.dfire.common.enums.StatusEnum;
import com.dfire.common.util.DateUtil;
import com.dfire.core.event.Dispatcher;
import com.dfire.core.event.HeraJobLostEvent;
import com.dfire.core.event.base.Events;
import com.dfire.core.event.handler.AbstractHandler;
import com.dfire.core.event.handler.JobHandler;
import com.dfire.logs.ScheduleLog;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * master 端负责执行的任务
 * 定时版本生成、清理、漏跑检查等操作
 * 晚点启动
 *
 * @author <a href="mailto:huoguo@2dfire.com">火锅</a>
 * @time 2018/11/6
 */
@Component()
@Order(5)
public class MasterTimerSchedule {


    @Autowired
    private MasterContext masterContext;

    /**
     * 漏泡检测，清理schedule线程，1小时调度一次,超过15分钟，job开始检测漏泡
     */
    @Scheduled(cron = "0 */30 * * * ?")
    private void lostJobCheck() {
        ScheduleLog.info("refresh host group success, start roll back");
        masterContext.refreshHostGroupCache();
        String currDate = DateUtil.getNowStringForAction();
        Dispatcher dispatcher = masterContext.getDispatcher();
        if (dispatcher != null) {
            Map<Long, HeraAction> actionMapNew = masterContext.getMaster().getHeraActionMap();
            Long tmp = Long.parseLong(currDate) - 15000000;
            if (actionMapNew != null && actionMapNew.size() > 0) {
                List<Long> actionIdList = new ArrayList<>();
                for (Long actionId : actionMapNew.keySet()) {
                    if (actionId < tmp) {
                        rollBackLostJob(actionId, actionMapNew, actionIdList);
                    }
                }
                ScheduleLog.info("roll back action count:" + actionIdList.size());
            }
            ScheduleLog.info("clear job scheduler ok");
        }
    }



    private void rollBackLostJob(Long actionId, Map<Long, HeraAction> actionMapNew, List<Long> actionIdList) {
        HeraAction lostJob = actionMapNew.get(actionId);
        if (lostJob != null) {
            String dependencies = lostJob.getDependencies();
            if (StringUtils.isNotBlank(dependencies)) {
                List<String> jobDependList = Arrays.asList(dependencies.split(","));
                boolean isAllComplete = true;
                HeraAction heraAction;
                String status;
                if (jobDependList.size() > 0) {
                    for (String jobDepend : jobDependList) {
                        Long jobDep = Long.parseLong(jobDepend);
                        if (actionMapNew.get(jobDep) != null) {
                            heraAction = actionMapNew.get(jobDep);
                            if (heraAction != null) {
                                status = heraAction.getStatus();
                                if (Constants.STATUS_WAIT.equals(status) || Constants.STATUS_FAILED.equals(status)) {
                                    isAllComplete = false;
                                }
                            }
                        }
                    }
                }
                if (isAllComplete) {
                    if (!actionIdList.contains(actionId)) {
                        masterContext.getDispatcher().forwardEvent(new HeraJobLostEvent(Events.UpdateJob, actionId.toString()));
                        actionIdList.add(actionId);
                        ScheduleLog.info("roll back lost actionId :" + actionId);
                    }
                }
            } else { //独立任务情况
                if (!actionIdList.contains(actionId)) {
                    masterContext.getDispatcher().forwardEvent(new HeraJobLostEvent(Events.UpdateJob, actionId.toString()));
                    actionIdList.add(actionId);
                    ScheduleLog.info("roll back lost actionId :" + actionId);
                }
            }
        }
    }


    @Scheduled(cron = "0 0 8 * * ?")
    private void removeJob() {
        ScheduleLog.warn("开始进行版本清理");
        Dispatcher dispatcher = masterContext.getDispatcher();
        Long currDate = Long.parseLong(DateUtil.getNowStringForAction());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, +1);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd0000000000");
        Long nextDay = Long.parseLong(simpleDateFormat.format(calendar.getTime()));
        Long tmp = currDate - 15000000;
        Map<Long, HeraAction> actionMapNew = masterContext.getMaster().getHeraActionMap();
        //移除未生成的调度
        List<AbstractHandler> handlers = dispatcher.getJobHandlers();
        List<JobHandler> shouldRemove = new ArrayList<>();
        if (handlers != null && handlers.size() > 0) {
            handlers.forEach(handler -> {
                JobHandler jobHandler = (JobHandler) handler;
                String actionId = jobHandler.getActionId();
                Long aid = Long.parseLong(actionId);
                if (Long.parseLong(actionId) < tmp) {
                    masterContext.getQuartzSchedulerService().deleteJob(actionId);
                } else if (aid >= currDate && aid < nextDay) {
                    if (!actionMapNew.containsKey(aid)) {
                        masterContext.getQuartzSchedulerService().deleteJob(actionId);
                        masterContext.getHeraJobActionService().delete(actionId);
                    }
                }
                if (!DateUtil.isToday(actionId)) {
                    shouldRemove.add(jobHandler);
                }
            });
        }
        //移除 过期 失效的handler
        shouldRemove.forEach(dispatcher::removeJobHandler);
        ScheduleLog.warn("版本清理完成");
    }

}
