package com.dfire.core.netty.master.response;

import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.entity.vo.HeraDebugHistoryVo;
import com.dfire.common.enums.StatusEnum;
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
import java.util.Iterator;
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
        //手动执行队列 查找该job是否存在
        Iterator<JobElement> iterator = context.getManualQueue().iterator();
        JobElement element;
        boolean hasRemove = false;
        while (iterator.hasNext()) {
            element = iterator.next();
            if (element.getJobId().equals(historyId)) {
                //从手动队列移除
                iterator.remove();
                hasRemove = true;
                webResponse = RpcWebResponse.WebResponse.newBuilder()
                        .setRid(request.getRid())
                        .setOperate(request.getOperate())
                        .setStatus(ResponseStatus.Status.OK)
                        .build();
                break;
            }
        }
        //如果从任务队列中已经找到  那么无需判断心跳
        if (!hasRemove) {
            for (MasterWorkHolder workHolder : context.getWorkMap().values()) {
                if (workHolder.getManningRunning().containsKey(historyId)) {
                    Future<RpcResponse.Response> future = new MasterHandleCancelJob().cancel(context,
                            workHolder.getChannel(), JobExecuteKind.ExecuteKind.ManualKind, historyId);
                    workHolder.getManningRunning().remove(historyId);
                    try {
                        future.get(1, TimeUnit.MINUTES);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    webResponse = RpcWebResponse.WebResponse.newBuilder()
                            .setRid(request.getRid())
                            .setOperate(request.getOperate())
                            .setStatus(ResponseStatus.Status.OK)
                            .build();
                    log.info("send web cancel response, actionId = " + historyId);
                }
            }
        }

        if (webResponse == null) {
            webResponse = RpcWebResponse.WebResponse.newBuilder()
                    .setRid(request.getRid())
                    .setOperate(request.getOperate())
                    .setStatus(ResponseStatus.Status.ERROR)
                    .setErrorText("Manual任务中找不到匹配的job(" + heraJobHistory.getJobId() + "," + heraJobHistory.getActionId() + ")，无法执行取消命令")
                    .build();
        }
        heraJobHistory.setIllustrate("任务取消");
        heraJobHistory.setEndTime(new Date());
        heraJobHistory.setStatus(StatusEnum.FAILED.toString());
        context.getHeraJobHistoryService().update(heraJobHistory);
        return webResponse;
    }

    private RpcWebResponse.WebResponse handleScheduleCancel(MasterContext context, RpcWebRequest.WebRequest request) {
        RpcWebResponse.WebResponse webResponse = null;
        String historyId = request.getId();
        HeraJobHistory heraJobHistory = context.getHeraJobHistoryService().findById(historyId);
        String jobId = heraJobHistory.getJobId();
        String actionId = heraJobHistory.getActionId();

        Iterator<JobElement> iterator = context.getScheduleQueue().iterator();
        JobElement jobElement;
        boolean hasRemove = false;
        while (iterator.hasNext()) {
            jobElement = iterator.next();
            if (jobElement.getJobId().equals(actionId)) {
                iterator.remove();
                hasRemove = true;
                webResponse = RpcWebResponse.WebResponse.newBuilder()
                        .setRid(request.getRid())
                        .setOperate(request.getOperate())
                        .setStatus(ResponseStatus.Status.OK)
                        .build();
                break;
            }
        }

        if (!hasRemove) {
            for (MasterWorkHolder workHolder : context.getWorkMap().values()) {
                if (workHolder.getRunning().containsKey(actionId)) {
                    Future<RpcResponse.Response> future = new MasterHandleCancelJob().cancel(context,
                            workHolder.getChannel(), JobExecuteKind.ExecuteKind.ScheduleKind, historyId);
                    workHolder.getRunning().remove(actionId);
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
            HeraJobHistory jobHistory = context.getHeraJobHistoryService().findById(jobId);
            jobHistory.setStatus(StatusEnum.WAIT.toString());
            context.getHeraJobHistoryService().updateHeraJobHistoryStatus(jobHistory);
        }
        heraJobHistory.setEndTime(new Date());
        heraJobHistory.setStatus(StatusEnum.FAILED.toString());
        heraJobHistory.setIllustrate("任务取消");
        context.getHeraJobHistoryService().update(heraJobHistory);
        return webResponse;
    }

}
