package com.dfire.core.netty.worker.request;

import com.dfire.common.entity.HeraDebugHistory;
import com.dfire.common.entity.model.HeraJobBean;
import com.dfire.common.entity.vo.HeraJobHistoryVo;
import com.dfire.common.util.BeanConvertUtils;
import com.dfire.common.vo.JobStatus;
import com.dfire.core.config.HeraGlobalEnvironment;
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
        } else if (request.getOperate() == Operate.Manual) {
            return manual(workContext, request);
        } else if(request.getOperate() == Operate.Schedule) {
            return schedule(workContext, request);
        }
        return null;
    }

    private Future<Response> manual(WorkContext workContext, Request request) {
        ExecuteMessage message = null;
        try {
            message = ExecuteMessage.newBuilder().mergeFrom(request.getBody()).build();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        final String jobId = message.getJobId();
        log.info("worker received master request to run manual job, jobId :" + jobId);
        if(workContext.getRunning().containsKey(jobId)) {
            log.info("job is running, can not run again, jobId :" + jobId);
            return workContext.getWorkThreadPool().submit(new Callable<Response>() {
                @Override
                public Response call() throws Exception {
                    return Response.newBuilder()
                            .setRid(request.getRid())
                            .setOperate(Operate.Schedule)
                            .setStatus(Status.ERROR)
                            .build();
                }
            });
        }

        final JobStatus jobStatus = workContext.getHeraGroupService().getJobStatus(jobId);
        final HeraJobHistoryVo history = workContext.getJobHistoryService().findJobHistory(jobStatus.getHistoryId());
        Future<Response> future = workContext.getWorkThreadPool().submit(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                history.setExecuteHost(WorkContext.host);
                history.setStartTime(new Date());
                workContext.getJobHistoryService().updateHeraJobHistory(BeanConvertUtils.convert(history));

                HeraJobBean jobBean = workContext.getHeraGroupService().getUpstreamJobBean(history.getJobId());
                String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                File directory = new File(HeraGlobalEnvironment.getDownloadDir()
                        + File.separator + date + File.separator + "manual-" + history.getId());
                if(!directory.exists()) {
                    directory.mkdir();
                }

                final Job job = JobUtils.createJob(new JobContext(JobContext.SCHEDULE_RUN), jobBean, history, directory.getAbsolutePath(), workContext.getApplicationContext());
                workContext.getManualRunning().put(jobId, job);

                Integer exitCode = -1;
                Exception exception = null;
                try {
                    exitCode = job.run();
                } catch (Exception e) {
                    exception = e;
                    history.getLog().appendHeraException(e);//此处应该执行更新日志操作
                } finally {
                    HeraJobHistoryVo heraJobHistory = workContext.getJobHistoryService().findJobHistory(history.getId());
                    heraJobHistory.setEndTime(new Date());
                    if(exitCode == 0) {
                        heraJobHistory.setStatus(com.dfire.common.enums.Status.SUCCESS);
                    } else {
                        heraJobHistory.setStatus(com.dfire.common.enums.Status.FAILED);
                    }
                    workContext.getJobHistoryService().updateHeraJobHistory(BeanConvertUtils.convert(heraJobHistory));
                    heraJobHistory.getLog().appendHera("exitCode=" + exitCode);

                    workContext.getJobHistoryService().updateHeraJobHistory(BeanConvertUtils.convert(heraJobHistory));

                    workContext.getManualRunning().remove(jobId);
                }

                Status status = Status.OK;
                String errorText = "";
                if(exitCode != 0) {
                    status = Status.ERROR;
                }
                if(exception != null) {
                    errorText = exception.getMessage();
                }

                Response response = Response.newBuilder()
                        .setRid(request.getRid())
                        .setOperate(Operate.Schedule)
                        .setStatus(status)
                        .setErrorText(errorText)
                        .build();
                log.info("send execute message, jobId = " + jobId);
                return response;
            }
        });

        return future;

    }

    private Future<Response> schedule(WorkContext workContext, Request request) {
        ExecuteMessage message = null;
        try {
            message = ExecuteMessage.newBuilder().mergeFrom(request.getBody()).build();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        final String jobId = message.getJobId();
        log.info("worker received master request to run schedule, jobId :" + jobId);
        if(workContext.getRunning().containsKey(jobId)) {
            log.info("job is running, can not run again, jobId :" + jobId);
            return workContext.getWorkThreadPool().submit(new Callable<Response>() {
                @Override
                public Response call() throws Exception {
                    return Response.newBuilder()
                            .setRid(request.getRid())
                            .setOperate(Operate.Schedule)
                            .setStatus(Status.ERROR)
                            .build();
                }
            });
        }

        final JobStatus jobStatus = workContext.getHeraGroupService().getJobStatus(jobId);
        final HeraJobHistoryVo history = workContext.getJobHistoryService().findJobHistory(jobStatus.getHistoryId());
        Future<Response> future = workContext.getWorkThreadPool().submit(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                history.setExecuteHost(WorkContext.host);
                history.setStartTime(new Date());
                workContext.getJobHistoryService().updateHeraJobHistory(BeanConvertUtils.convert(history));

                HeraJobBean jobBean = workContext.getHeraGroupService().getUpstreamJobBean(history.getJobId());
                String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                File directory = new File(HeraGlobalEnvironment.getDownloadDir()
                        + File.separator + date + File.separator + history.getId());
                if(!directory.exists()) {
                    directory.mkdir();
                }

                final Job job = JobUtils.createJob(new JobContext(JobContext.SCHEDULE_RUN), jobBean, history, directory.getAbsolutePath(), workContext.getApplicationContext());
                workContext.getRunning().put(jobId, job);

                Integer exitCode = -1;
                Exception exception = null;
                try {
                    exitCode = job.run();
                } catch (Exception e) {
                    exception = e;
                    history.getLog().appendHeraException(e);//此处应该执行更新日志操作
                } finally {
                    HeraJobHistoryVo heraJobHistory = workContext.getJobHistoryService().findJobHistory(history.getId());
                    heraJobHistory.setEndTime(new Date());
                    if(exitCode == 0) {
                        heraJobHistory.setStatus(com.dfire.common.enums.Status.SUCCESS);
                    } else {
                        heraJobHistory.setStatus(com.dfire.common.enums.Status.FAILED);
                    }
                    workContext.getJobHistoryService().updateHeraJobHistory(BeanConvertUtils.convert(heraJobHistory));
                    heraJobHistory.getLog().appendHera("exitCode=" + exitCode);

                    workContext.getJobHistoryService().updateHeraJobHistory(BeanConvertUtils.convert(heraJobHistory));

                    workContext.getRunning().remove(jobId);
                }

                Status status = Status.OK;
                String errorText = "";
                if(exitCode != 0) {
                    status = Status.ERROR;
                }
                if(exception != null) {
                    errorText = exception.getMessage();
                }

                Response response = Response.newBuilder()
                        .setRid(request.getRid())
                        .setOperate(Operate.Schedule)
                        .setStatus(status)
                        .setErrorText(errorText)
                        .build();
                log.info("send execute message, jobId = " + jobId);
                return response;
            }
        });

        return future;

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
                File directory = new File(HeraGlobalEnvironment.getDownloadDir() + File.separator + date + File.separator);
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
                    if(exitCode == 0) debugHistory.setStatus(com.dfire.common.enums.Status.SUCCESS);
                    else {
                        debugHistory.setStatus(com.dfire.common.enums.Status.FAILED);
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
