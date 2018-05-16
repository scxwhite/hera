package com.dfire.core.netty.worker.request;

import com.dfire.common.entity.HeraDebugHistory;
import com.dfire.common.entity.vo.HeraJobHistoryVo;
import com.dfire.core.message.Protocol.*;
import com.dfire.core.netty.worker.WorkContext;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午10:57 2018/5/11
 * @desc
 */
@Slf4j
public class WorkHandleCancel {

    public Future<Response> handleCancel(final WorkContext workContext, final Request request) {
        try {
            CancelMessage cancelMessage = CancelMessage.newBuilder()
                    .mergeFrom(request.getBody())
                    .build();
            if(cancelMessage.getEk() == ExecuteKind.DebugKind) {
                return cancelDebug(workContext, request, cancelMessage.getId());
            } else if(cancelMessage.getEk() == ExecuteKind.ScheduleKind) {
                return cancelSchedule(workContext, request, cancelMessage.getId());
            } else if(cancelMessage.getEk() == ExecuteKind.ManualKind) {
                return cancelManual(workContext, request, cancelMessage.getId());
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Future<Response> cancelManual(WorkContext workContext, Request request, String historyId) {
        HeraJobHistoryVo history = workContext.getJobHistoryService().findJobHistory(historyId);
        final String jobId = history.getJobId();
        log.info("worker receive cancel manual job, jobId =" + jobId);
        if(!workContext.getManualRunning().containsKey(history.getId())) {
            return workContext.getWorkThreadPool().submit(new Callable<Response>() {
                @Override
                public Response call() throws Exception {
                    return Response.newBuilder()
                            .setRid(request.getRid())
                            .setOperate(Operate.Cancel)
                            .setStatus(Status.ERROR)
                            .setErrorText("运行任务中查无此任务")
                            .build();
                }
            });
        }
        return workContext.getWorkThreadPool().submit(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                workContext.getWorkClient().cancelManualJob(historyId);
                return Response.newBuilder()
                        .setRid(request.getRid())
                        .setOperate(Operate.Cancel)
                        .setStatus(Status.OK)
                        .build();
            }
        });
    }

    private Future<Response> cancelSchedule(WorkContext workContext, Request request, String historyId) {
        HeraJobHistoryVo history = workContext.getJobHistoryService().findJobHistory(historyId);
        final String jobId = history.getJobId();
        log.info("worker receive cancel schedule job, jobId =" + jobId);
        if(!workContext.getRunning().containsKey(history.getId())) {
            return workContext.getWorkThreadPool().submit(new Callable<Response>() {
                @Override
                public Response call() throws Exception {
                    return Response.newBuilder()
                            .setRid(request.getRid())
                            .setOperate(Operate.Cancel)
                            .setStatus(Status.ERROR)
                            .setErrorText("运行任务中查无此任务")
                            .build();
                }
            });
        }
        return workContext.getWorkThreadPool().submit(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                workContext.getWorkClient().cancelScheduleJob(jobId);
                return Response.newBuilder()
                        .setRid(request.getRid())
                        .setOperate(Operate.Cancel)
                        .setStatus(Status.OK)
                        .build();
            }
        });
    }

    private Future<Response> cancelDebug(WorkContext workContext, Request request, String debugId) {
        Future<Response> future = null;
        if(!workContext.getDebugRunning().containsKey(debugId)) {
            future = workContext.getWorkThreadPool().submit(new Callable<Response>() {
                @Override
                public Response call() throws Exception {
                    return Response.newBuilder()
                            .setRid(request.getRid())
                            .setOperate(Operate.Cancel)
                            .setStatus(Status.ERROR)
                            .setErrorText("运行任务中查无此任务")
                            .build();
                }
            });
            HeraDebugHistory debugHistory = workContext.getDebugHistoryService().findDebugHistoryById(debugId);
            debugHistory.setStatus(com.dfire.common.enums.Status.FAILED);
            debugHistory.setEndTime(new Date());
            workContext.getDebugHistoryService().update(debugHistory);
        } else {
            future = workContext.getWorkThreadPool().submit(new Callable<Response>() {
                @Override
                public Response call() throws Exception {
                    workContext.getWorkClient().cancelDebugJob(debugId);
                    return Response.newBuilder()
                            .setRid(request.getRid())
                            .setOperate(Operate.Cancel)
                            .setStatus(Status.OK)
                            .build();
                }
            });
        }
        return future;
    }

}
