package com.dfire.core.netty.master;


import com.dfire.common.constant.Status;
import com.dfire.common.entity.HeraAction;
import com.dfire.common.entity.HeraDebugHistory;
import com.dfire.common.entity.HeraGroup;
import com.dfire.common.vo.HeraHostGroupVo;
import com.dfire.core.HeraException;
import com.dfire.core.config.HeraGlobalEnvironment;
import com.dfire.core.event.HeraDebugFailEvent;
import com.dfire.core.event.HeraDebugSuccessEvent;
import com.dfire.core.event.base.Events;
import com.dfire.core.event.listenter.*;
import com.dfire.core.message.HeartBeatInfo;
import com.dfire.core.message.Protocol;
import com.dfire.core.message.Protocol.*;
import com.dfire.core.netty.master.response.MasterExecuteJob;
import com.dfire.core.queue.JobElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:24 2018/1/12
 * @desc
 */
@Component
@Slf4j
public class Master {

    @Autowired
    private MasterContext masterContext;
    private Map<Long, HeraAction> heraActionMap;

    public Master(final MasterContext masterContext) {
        this.masterContext = masterContext;
//        HeraGroup globalGroup = masterContext.getHeraGroupService().getGlobalGroup();

        if (HeraGlobalEnvironment.env.equalsIgnoreCase("pre")) {
            //预发环境不执行调度
            masterContext.getDispatcher().addDispatcherListener(new HeraStopScheduleJobListener());
        }

        masterContext.getDispatcher().addDispatcherListener(new HeraAddJobListener(this, masterContext));
        masterContext.getDispatcher().addDispatcherListener(new HeraJobFailListener(masterContext));
        masterContext.getDispatcher().addDispatcherListener(new HeraDebugListener(masterContext));
        masterContext.getDispatcher().addDispatcherListener(new HeraJobSuccessListener(masterContext));

        masterContext.getDispatcher().forwardEvent(Events.Initialize);
        masterContext.setMaster(this);
        masterContext.refreshHostGroupCache();
        log.info("refresh hostGroup cache");

        masterContext.getSchedulePool().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    scan();
                } catch (Exception e) {
                    log.error("scan queue exception");
                }

            }
        }, 0, HeraGlobalEnvironment.getScanExceptionRate(), TimeUnit.MICROSECONDS);


    }

    public void scan() {
        if (!masterContext.getDebugQueue().isEmpty()) {
            final JobElement element = masterContext.getDebugQueue().poll();
            MasterWorkHolder selectWork = getRunnableWork(element.getHostGroupId());
            if (selectWork != null) {
                masterContext.getDebugQueue().offer(element);
            } else {
                runDebugJob(selectWork, element.getJobId());
            }
        }

    }

    private void runDebugJob(MasterWorkHolder selectWork, String jobId) {
        final MasterWorkHolder workHolder = selectWork;
        new Thread() {
            @Override
            public void run() {
                HeraDebugHistory history = masterContext.getHeraDebugHistoryService().findDebugHistoryById(jobId);
                history.getLog().append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 开始运行");
                masterContext.getHeraDebugHistoryService().update(history);
                Exception exception = null;
                Response response = null;
                try {
                    Future<Response> future = new MasterExecuteJob().executeJob(masterContext, workHolder, ExecuteKind.DebugKind, jobId);
                    response = future.get();
                } catch (Exception e) {
                    exception = e;
                    log.error(String.format("debugId:%s run failed", jobId), e);
                }
                boolean success = response.getStatus() == Protocol.Status.OK ? true : false;
                if (!success) {
                    exception = new HeraException(String.format("fileId:%s run failed ", history.getFileId()), exception);
                    log.info("debug job error");
                    history = masterContext.getHeraDebugHistoryService().findDebugHistoryById(jobId);
                    HeraDebugFailEvent failEvent = HeraDebugFailEvent.builder()
                            .debugHistory(history)
                            .throwable(exception)
                            .fileId(history.getFileId())
                            .build();
                    masterContext.getDispatcher().forwardEvent(failEvent);
                } else {
                    log.info("debug success");
                    HeraDebugSuccessEvent successEvent = HeraDebugSuccessEvent.builder()
                            .fileId(history.getFileId())
                            .history(history)
                            .build();
                    masterContext.getDispatcher().forwardEvent(successEvent);
                }
            }
        }.start();

    }


    private MasterWorkHolder getRunnableWork(String hostGroupId) {
        if (hostGroupId == null) {
            hostGroupId = HeraGlobalEnvironment.defaultWorkerGroup;
        }
        MasterWorkHolder workHolder = null;
        if (masterContext.getHostGroupCache() != null) {
            HeraHostGroupVo hostGroupCache = masterContext.getHostGroupCache().get(hostGroupId);
            List<String> hosts = hostGroupCache.getHosts();
            if (hostGroupCache != null && hosts != null && hosts.size() > 0) {
                int size = hosts.size();
                for (int i = 0; i < size && workHolder == null; i++) {
                    String host = hostGroupCache.selectHost();
                    if (host == null) {
                        break;
                    }
                    for (MasterWorkHolder worker : masterContext.getWorkMap().values()) {
                        if (worker != null && worker.heartBeatInfo != null && worker.heartBeatInfo.host.trim().equals(host.trim())) {
                            HeartBeatInfo heartBeatInfo = worker.heartBeatInfo;
                            if (heartBeatInfo.getMemRate() != null && heartBeatInfo.getCpuLoadPerCore() != null
                                    && heartBeatInfo.getMemRate() < HeraGlobalEnvironment.getMaxMemRate() && heartBeatInfo.getCpuLoadPerCore() < HeraGlobalEnvironment.getMaxCpuLoadPerCore()) {

                                Float assignTaskNum = (heartBeatInfo.getMemTotal() - HeraGlobalEnvironment.getMaxCpuLoadPerCore()) / HeraGlobalEnvironment.getMaxCpuLoadPerCore();
                                int sum = heartBeatInfo.debugRunning.size() + heartBeatInfo.manualRunning.size() + heartBeatInfo.running.size();
                                if (assignTaskNum.intValue() > sum) {
                                    workHolder = worker;
                                    break;
                                }
                            }
                        } else {
                            if (worker == null) {
                                log.error("worker is null");
                            } else if (worker != null && worker.getHeartBeatInfo() == null && worker.getChannel() != null) {
                                log.error("worker " + worker.getChannel().toString() + "heart is null");
                            }
                        }
                    }
                }
            }
        }
        return workHolder;
    }

    public void debug(HeraDebugHistory debugHistory) {
        JobElement element = JobElement.builder()
                .jobId(debugHistory.getId())
                .hostGroupId(debugHistory.getHostGroupId())
                .build();
        debugHistory.setStatus(Status.RUNNING);
        debugHistory.setStartTime(new Date());
        debugHistory.getLog().append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 进入任务队列");
        masterContext.getHeraDebugHistoryService().update(debugHistory);
        masterContext.getDebugQueue().offer(element);

    }
}
