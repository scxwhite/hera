package com.dfire.core.netty.worker;


import com.dfire.common.entity.HeraDebugHistory;
import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.vo.JobStatus;
import com.dfire.core.config.HeraGlobalEnvironment;
import com.dfire.core.job.Job;
import com.dfire.core.lock.DistributeLock;
import com.dfire.core.message.Protocol.*;
import com.dfire.core.netty.worker.request.WorkHandleWebCancel;
import com.dfire.core.netty.worker.request.WorkHandleWebUpdate;
import com.dfire.core.netty.worker.request.WorkerHeartBeat;
import com.dfire.core.netty.worker.request.WorkerHandleWebExecute;
import com.dfire.core.schedule.ScheduleInfoLog;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.*;


/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 10:34 2018/1/10
 * @desc
 */
@Slf4j
@Data
@Component
public class WorkClient {

    private Bootstrap bootstrap;
    private EventLoopGroup eventLoopGroup;
    private WorkContext workContext = new WorkContext();
    private ScheduledExecutorService service;

    @Autowired
    WorkerHandleWebExecute workerWebExecute;

    @Autowired
    public void WorkClient() {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ProtobufVarint32FrameDecoder())
                                .addLast(new ProtobufDecoder(SocketMessage.getDefaultInstance()))
                                .addLast(new ProtobufVarint32LengthFieldPrepender())
                                .addLast(new ProtobufEncoder())
                                .addLast(new WorkHandler(workContext));
                    }
                });
        log.info("start work client success ");
        workContext.setWorkClient(this);

        service = Executors.newScheduledThreadPool(2);
        service.scheduleAtFixedRate(new Runnable() {
            private WorkerHeartBeat heartBeat = new WorkerHeartBeat();
            private int failCount = 0;

            @Override
            public void run() {
                try {
                    if (workContext.getServerChannel() != null) {
                        ChannelFuture channelFuture = heartBeat.send(workContext);
                        channelFuture.addListener((future) -> {
                            if (!future.isSuccess()) {
                                failCount++;
                                log.info("send heart beat failed ,failCount :" + failCount);
                            } else {
                                failCount = 0;
                                log.info("send heart beat success");
                            }
                            if (failCount > 10) {
                                future.cancel(true);
                                log.info("cancel connect server ,failCount:" + failCount);
                            }
                        });
                    } else {
                        log.info("server channel can not find on " + DistributeLock.host);
                    }
                } catch (Exception e) {
                    log.info("heart beat send failed ：" + failCount);
                    log.error("heart beat error:", e);
                }
            }

        }, 5, 5, TimeUnit.SECONDS);

        service.scheduleAtFixedRate(new Runnable() {

            private void editLog(Job job, Exception e) {
                try {
                    HeraJobHistory his = job.getJobContext().getHeraJobHistory();
                    String logContent = his.getLog().getContent();
                    if (logContent == null) {
                        logContent = "";
                    }
                    log.error(new StringBuilder("log output error!\n")
                            .append("[jobId:").append(his.getJobId())
                            .append(", hisId:").append(his.getId())
                            .append(", logLength:")
                            .append(logContent.length()).append("]")
                            .toString(), e);
                } catch (Exception ex) {
                    log.error("log exception error!");
                }
            }


            private void editDebugLog(Job job, Exception e) {
                try {
                    HeraDebugHistory history = job.getJobContext().getDebugHistory();
                    String logContent = history.getLog().getContent();
                    if (logContent == null) {
                        logContent = "";
                    }
                    log.error(new StringBuilder("log output error!\n")
                            .append("[fileId:").append(history.getFileId())
                            .append(", hisId:").append(history.getId())
                            .append(", logLength:")
                            .append(logContent.length()).append("]")
                            .toString(), e);
                } catch (Exception ex) {
                    log.error("log exception error!");
                }
            }

            @Override
            public void run() {

                for (Job job : new HashSet<Job>(workContext.getRunning().values())) {
                    try {
                        HeraDebugHistory history = job.getJobContext().getDebugHistory();
                        workContext.getDebugHistoryService().update(history);
                    } catch (Exception e) {
                        editDebugLog(job, e);
                    }
                }

                for (Job job : new HashSet<Job>(workContext.getManualRunning().values())) {
                    try {
                        HeraDebugHistory history = job.getJobContext().getDebugHistory();
                        workContext.getDebugHistoryService().update(history);
                    } catch (Exception e) {
                        editLog(job, e);
                    }
                }

                for (Job job : new HashSet<Job>(workContext.getDebugRunning().values())) {
                    try {
                        HeraDebugHistory history = job.getJobContext().getDebugHistory();
                        workContext.getDebugHistoryService().update(history);
                    } catch (Exception e) {
                        editDebugLog(job, e);
                    }
                }
            }
        }, 0, 3, TimeUnit.SECONDS);

    }

    public synchronized void connect(String host) throws Exception {
        //首先判断服务频道是否开启
        if (workContext.getServerChannel() != null) {
            //如果已经与服务端连接
            if (workContext.getServerHost().equals(host)) {
                return;
            } else { //关闭之前通信
                workContext.getServerChannel().close();
                workContext.setServerChannel(null);
            }
        }
        workContext.setServerHost(host);
        CountDownLatch latch = new CountDownLatch(1);
        ChannelFutureListener futureListener = (future) -> {
            try {
                if (future.isSuccess()) {
                    workContext.setServerChannel(future.channel());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        };
        ChannelFuture connectFuture = bootstrap.connect(new InetSocketAddress(host, HeraGlobalEnvironment.getConnectPort()));
        connectFuture.addListener(futureListener);
        if (!latch.await(2, TimeUnit.SECONDS)) {
            connectFuture.removeListener(futureListener);
            connectFuture.cancel(true);
            throw new ExecutionException(new TimeoutException("connect server consumption of 2 seconds"));
        }
        if (!connectFuture.isSuccess()) {
            throw new RuntimeException("connect server failed " + host,
                    connectFuture.cause());
        }
        ScheduleInfoLog.info("connect server success");
    }

    public void cancelDebugJob(String debugId) {
        Job job = workContext.getDebugRunning().get(debugId);
        job.cancel();
        workContext.getDebugRunning().remove(debugId);

        HeraDebugHistory history = job.getJobContext().getDebugHistory();
        history.setEndTime(new Date());
        history.setStatus(com.dfire.common.constant.Status.FAILED);
        workContext.getDebugHistoryService().update(history);
        history.getLog().appendHera("任务被取消");
        workContext.getDebugHistoryService().update(history);


    }

    public void cancelManualJob(String historyId) {
        Job job = workContext.getManualRunning().get(historyId);
        workContext.getManualRunning().remove(historyId);
        job.cancel();

        HeraJobHistory history = job.getJobContext().getHeraJobHistory();
        history.setEndTime(new Date());
        String illustrate = history.getIllustrate();
        if(illustrate!=null && illustrate.trim().length()>0){
            history.setIllustrate(illustrate+"；手动取消该任务");
        }else{
            history.setIllustrate("手动取消该任务");
        }
        history.setStatus(com.dfire.common.constant.Status.FAILED);
        workContext.getJobHistoryService().updateHeraJobHistory(history);
        history.getLog().appendHera("任务被取消");
        workContext.getJobHistoryService().updateHeraJobHistory(history);

    }

    public void cancelScheduleJob(String jobId) {
        Job job = workContext.getRunning().get(jobId);
        workContext.getRunning().remove(jobId);
        job.cancel();

        HeraJobHistory history = job.getJobContext().getHeraJobHistory();
        history.setEndTime(new Date());
        String illustrate = history.getIllustrate();
        if(illustrate!=null && illustrate.trim().length()>0){
            history.setIllustrate(illustrate+"；手动取消该任务");
        }else{
            history.setIllustrate("手动取消该任务");
        }
        history.setStatus(com.dfire.common.constant.Status.FAILED);
        workContext.getJobHistoryService().updateHeraJobHistory(history);
        history.getLog().appendHera("任务被取消");
        workContext.getJobHistoryService().updateHeraJobHistory(history);

    }


    public void executeJobFromWeb(ExecuteKind kind, String id) throws ExecutionException, InterruptedException {
        WebResponse response = workerWebExecute.handleWebExecute(workContext, kind, id).get();
        if (response.getStatus() == Status.ERROR) {
            log.error("netty manual web request get jobStatus error");
        }
    }

    public void cancelJobFromWeb(ExecuteKind kind, String id) throws ExecutionException, InterruptedException {
        WebResponse webResponse = new WorkHandleWebCancel().handleCancel(workContext, kind, id).get();
        if(webResponse.getStatus() == Status.ERROR) {
            log.error("cancel from web exception");
        }

    }

    public void updateJobFromWeb(String jobId) throws ExecutionException, InterruptedException {
        WebResponse webResponse = new WorkHandleWebUpdate().handleUpdate(workContext, jobId).get();
        if(webResponse.getStatus() == Status.ERROR) {
            log.error("cancel from web exception");
        }

    }


}
