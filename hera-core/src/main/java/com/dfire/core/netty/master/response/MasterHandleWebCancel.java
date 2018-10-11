package com.dfire.core.netty.master.response;

import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.entity.vo.HeraDebugHistoryVo;
import com.dfire.common.entity.vo.HeraJobHistoryVo;
import com.dfire.common.enums.StatusEnum;
import com.dfire.common.enums.TriggerTypeEnum;
import com.dfire.common.util.BeanConvertUtils;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.core.netty.master.MasterWorkHolder;
import com.dfire.core.queue.JobElement;
import com.dfire.protocol.*;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午3:58 2018/5/11
 * @desc 取消任务，master查询出任务所在的worker channel,发起取消任务请求
 */
@Slf4j
public class MasterHandleWebCancel {

    public RpcWebResponse.WebResponse handleWebCancel(MasterContext masterContext, RpcWebRequest.WebRequest request) {
        if (request.getEk() == JobExecuteKind.ExecuteKind.ScheduleKind) {
            return handleScheduleCancel(masterContext, request);
        } else if (request.getEk() == JobExecuteKind.ExecuteKind.ManualKind) {
            return handleManualCancel(masterContext, request);
        } else if (request.getEk() == JobExecuteKind.ExecuteKind.DebugKind) {
            return handleDebugCancel(masterContext, request);
        }
        return null;
    }

    private RpcWebResponse.WebResponse handleDebugCancel(MasterContext context, RpcWebRequest.WebRequest request) {
        RpcWebResponse.WebResponse webResponse = null;
        String debugId = request.getId();
        HeraDebugHistoryVo debugHistory = context.getHeraDebugHistoryService().findById(debugId);
        for (JobElement element : new ArrayList<>(context.getDebugQueue())) {
            if (element.getJobId().equals(debugId)) {
                webResponse = RpcWebResponse.WebResponse.newBuilder()
                        .setRid(request.getRid())
                        .setOperate(request.getOperate())
                        .setStatus(ResponseStatus.Status.OK)
                        .build();
                debugHistory.getLog().appendHera("任务取消");
                context.getHeraDebugHistoryService().update(BeanConvertUtils.convert(debugHistory));
                break;

            }
        }

        for (Channel key : new HashSet<>(context.getWorkMap().keySet())) {
            MasterWorkHolder workHolder = context.getWorkMap().get(key);
            if (workHolder.getDebugRunning().containsKey(debugId)) {
                Future<RpcResponse.Response> future = new MasterHandleCancelJob().cancel(context,
                        workHolder.getChannel(), JobExecuteKind.ExecuteKind.DebugKind, debugId);
                workHolder.getDebugRunning().remove(debugId);
                try {
                    future.get(10, TimeUnit.SECONDS);
                } catch (Exception e) {

                }
                webResponse = RpcWebResponse.WebResponse.newBuilder()
                        .setRid(request.getRid())
                        .setOperate(request.getOperate())
                        .setStatus(ResponseStatus.Status.OK)
                        .build();

                log.info("send web cancel response, actionId = " + debugId);
            }
        }

        if (webResponse != null) {
            webResponse = RpcWebResponse.WebResponse.newBuilder()
                    .setRid(request.getRid())
                    .setOperate(request.getOperate())
                    .setStatus(ResponseStatus.Status.ERROR)
                    .setErrorText("Manual任务中找不到匹配的job(" + debugHistory.getId() + "," + debugHistory.getId() + ")，无法执行取消命令")
                    .build();
        }
        debugHistory = context.getHeraDebugHistoryService().findById(debugId);
        debugHistory.setEndTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        debugHistory.setStatus(StatusEnum.FAILED);
        context.getHeraDebugHistoryService().update(BeanConvertUtils.convert(debugHistory));
        return webResponse;


    }

    private RpcWebResponse.WebResponse handleManualCancel(MasterContext context, RpcWebRequest.WebRequest request) {
        RpcWebResponse.WebResponse webResponse = null;
        String historyId = request.getId();
        HeraJobHistory heraJobHistory = context.getHeraJobHistoryService().findById(historyId);
        HeraJobHistoryVo history = BeanConvertUtils.convert(heraJobHistory);
        String jobId = history.getJobId();
        for (JobElement element : new ArrayList<>(context.getManualQueue())) {
            if (element.getJobId().equals(historyId)) {
                webResponse = RpcWebResponse.WebResponse.newBuilder()
                        .setRid(request.getRid())
                        .setOperate(request.getOperate())
                        .setStatus(ResponseStatus.Status.OK)
                        .build();
                history.getLog().appendHera("任务取消");
                context.getHeraJobHistoryService().updateHeraJobHistoryLog(BeanConvertUtils.convert(history));
                break;

            }
        }

        if (history.getTriggerType() == TriggerTypeEnum.MANUAL) {
            for (Channel key : new HashSet<>(context.getWorkMap().keySet())) {
                MasterWorkHolder workHolder = context.getWorkMap().get(key);
                if (workHolder.getManningRunning().containsKey(historyId)) {
                    Future<RpcResponse.Response> future = new MasterHandleCancelJob().cancel(context,
                            workHolder.getChannel(), JobExecuteKind.ExecuteKind.ManualKind, historyId);
                    workHolder.getManningRunning().remove(jobId);
                    try {
                        future.get(10, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    webResponse = RpcWebResponse.WebResponse.newBuilder()
                            .setRid(request.getRid())
                            .setOperate(request.getOperate())
                            .setStatus(ResponseStatus.Status.OK)
                            .build();

                    log.info("send web cancel response, actionId = " + jobId);
                }
            }
        }

        if (webResponse != null) {
            webResponse = RpcWebResponse.WebResponse.newBuilder()
                    .setRid(request.getRid())
                    .setOperate(request.getOperate())
                    .setStatus(ResponseStatus.Status.ERROR)
                    .setErrorText("Manual任务中找不到匹配的job(" + history.getJobId() + "," + history.getId() + ")，无法执行取消命令")
                    .build();
        }
        heraJobHistory = context.getHeraJobHistoryService().findById(historyId);
        history = BeanConvertUtils.convert(heraJobHistory);
        history.setEndTime(new Date());
        history.setStatusEnum(StatusEnum.FAILED);
        context.getHeraJobHistoryService().update(BeanConvertUtils.convert(history));
        return webResponse;

    }

    private RpcWebResponse.WebResponse handleScheduleCancel(MasterContext context, RpcWebRequest.WebRequest request) {
        RpcWebResponse.WebResponse webResponse = null;
        String historyId = request.getId();
        HeraJobHistory heraJobHistory = context.getHeraJobHistoryService().findById(historyId);
        HeraJobHistoryVo history = BeanConvertUtils.convert(heraJobHistory);
        String jobId = history.getJobId();
        for (JobElement element : new ArrayList<>(context.getScheduleQueue())) {
            if (element.getJobId().equals(historyId)) {
                webResponse = RpcWebResponse.WebResponse.newBuilder()
                        .setRid(request.getRid())
                        .setOperate(request.getOperate())
                        .setStatus(ResponseStatus.Status.OK)
                        .build();
                history.getLog().appendHera("任务取消");
                context.getHeraJobHistoryService().updateHeraJobHistoryLog(BeanConvertUtils.convert(history));
                break;

            }
        }

        for (Channel key : new HashSet<>(context.getWorkMap().keySet())) {
            MasterWorkHolder workHolder = context.getWorkMap().get(key);
            if (workHolder.getRunning().containsKey(jobId)) {
                Future<RpcResponse.Response> future = new MasterHandleCancelJob().cancel(context,
                        workHolder.getChannel(), JobExecuteKind.ExecuteKind.ScheduleKind, historyId);
                workHolder.getRunning().remove(jobId);
                try {
                    future.get(10, TimeUnit.SECONDS);
                } catch (Exception e) {

                }
                webResponse = RpcWebResponse.WebResponse.newBuilder()
                        .setRid(request.getRid())
                        .setOperate(request.getOperate())
                        .setStatus(ResponseStatus.Status.OK)
                        .build();

                log.info("send web cancel response, actionId = " + jobId);
            }
        }

        if (webResponse != null) {
            HeraJobHistory jobHistory = context.getHeraJobHistoryService().findById(jobId);
            jobHistory.setStatus(StatusEnum.WAIT.toString());
            context.getHeraJobHistoryService().updateHeraJobHistoryStatus(jobHistory);
        }
        heraJobHistory = context.getHeraJobHistoryService().findById(historyId);
        history = BeanConvertUtils.convert(heraJobHistory);
        history.setEndTime(new Date());
        history.setStatusEnum(StatusEnum.FAILED);
        context.getHeraJobHistoryService().update(BeanConvertUtils.convert(history));
        return webResponse;
    }


}
