package com.dfire.core.netty.worker.request;

import com.dfire.common.entity.HeraDebugHistory;
import com.dfire.core.job.Job;
import com.dfire.core.job.JobContext;
import com.dfire.core.message.Protocol.*;
import com.dfire.core.netty.worker.WorkContext;
import com.dfire.core.util.JobUtils;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:20 2018/4/25
 * @desc
 */
@Slf4j
public class WorkExecuteJob {

    public Future<Response> execute(final WorkContext workContext, final Request request) {
        if (request.getOperate() == Operate.Debug) {
            return debug(workContext, request);
        }
        return null;
    }

    private Future<Response> debug(WorkContext workContext, Request request) {
        DebugMessage debugMessage = null;
        try {
            debugMessage = DebugMessage.newBuilder().mergeFrom(request.getBody()).build();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        String debugId = debugMessage.getDebugId();
        HeraDebugHistory history = workContext.getDebugHistoryService().findDebugHistoryById(debugId);
        Future<Response> future = workContext.getWorkThreadPool().submit(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                history.setExecuteHost(WorkContext.host);
                history.setStartTime(new Date());
                workContext.getDebugHistoryService().update(history);

                String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                File directory = new File(File.separator + date + File.separator);
                if(!directory.exists()) {
                    directory.mkdirs();
                }
                Job job = JobUtils.createDebugJob(new JobContext(JobContext.DEBUG_RUN), history,
                        directory.getAbsolutePath(), workContext.getApplicationContext());
                workContext.getDebugRunning().put(debugId, job);

                int exitCode = -1;
                Exception exception = null;
                try {
                    exitCode = job.run();
                } catch (Exception e) {
                    exception = e;
                    history.getLog().appendHeraException(e);
                } finally {
                    HeraDebugHistory debugHistory = workContext.getDebugHistoryService().findDebugHistoryById(history.getId());
                    debugHistory.setEndTime(new Date());
                    if(exitCode == 0) debugHistory.setStatus(com.dfire.common.constant.Status.SUCCESS);
                    else {
                        debugHistory.setStatus(com.dfire.common.constant.Status.FAILED);
                    }
                    history.getLog().appendHera("exitCode =" + exitCode);
                    workContext.getDebugHistoryService().update(debugHistory);
                    log.info("update debug jobId = " + debugId + " success");

                }
                Status status = Status.OK;
                String errorText = "";
                if(exitCode != 0) {
                    status = Status.ERROR;
                }
                if(exception != null && exception.getMessage() != null) {
                    errorText = exception.getMessage();
                }
                Response response = Response.newBuilder()
                        .setRid(request.getRid())
                        .setOperate(Operate.Debug)
                        .setStatus(status)
                        .setErrorText(errorText)
                        .build();
                return response;
            }
        });
        return  future;
    }

}
