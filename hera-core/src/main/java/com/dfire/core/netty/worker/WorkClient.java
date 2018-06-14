package com.dfire.core.netty.worker;


import com.dfire.common.entity.vo.HeraDebugHistoryVo;
import com.dfire.common.entity.vo.HeraJobHistoryVo;
import com.dfire.common.util.BeanConvertUtils;
import com.dfire.core.config.HeraGlobalEnvironment;
import com.dfire.core.job.Job;
import com.dfire.core.lock.DistributeLock;
import com.dfire.core.message.Protocol.ExecuteKind;
import com.dfire.core.message.Protocol.SocketMessage;
import com.dfire.core.message.Protocol.Status;
import com.dfire.core.message.Protocol.WebResponse;
import com.dfire.core.netty.worker.request.WorkHandleWebCancel;
import com.dfire.core.netty.worker.request.WorkHandleWebUpdate;
import com.dfire.core.netty.worker.request.WorkerHandleWebExecute;
import com.dfire.core.netty.worker.request.WorkerHandlerHeartBeat;
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
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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

    /**
     * 客户端引导，发起连接
     */
    private Bootstrap bootstrap;
    private EventLoopGroup eventLoopGroup;
    private WorkContext workContext = new WorkContext();
    private ScheduledExecutorService service;
    public final Timer workClientTimer = new HashedWheelTimer(Executors.defaultThreadFactory(), 5, TimeUnit.SECONDS);

    /**
     * ProtobufVarint32LengthFieldPrepender:对protobuf协议的的消息头上加上一个长度为32的整形字段,用于标志这个消息的长度。
     * <p>
     * ProtobufVarint32FrameDecoder:针对protobuf协议的ProtobufVarint32LengthFieldPrepender()所加的长度属性的解码器
     */
    @Autowired
    public void WorkClient(ApplicationContext applicationContext) {
        workContext.setWorkClient(this);
        workContext.setApplicationContext(applicationContext);
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new IdleStateHandler(0, 0, 5, TimeUnit.SECONDS))
                                .addLast("frameDecoder", new ProtobufVarint32FrameDecoder())
                                .addLast("decoder", new ProtobufDecoder(SocketMessage.getDefaultInstance()))
                                .addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender())
                                .addLast("encoder", new ProtobufEncoder())
                                .addLast(new WorkHandler(workContext));
                    }
                });
        log.info("start work client success ");


        TimerTask heartBeatTask = new TimerTask() {

            private WorkerHandlerHeartBeat workerHandlerHeartBeat = new WorkerHandlerHeartBeat();
            private int failCount = 0;

            @Override
            public void run(Timeout timeout) throws Exception {
                try {
                    if (workContext.getServerChannel() != null) {
                        ChannelFuture channelFuture = workerHandlerHeartBeat.send(workContext);
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
                } finally {
                    workClientTimer.newTimeout(this, 3, TimeUnit.SECONDS);
                }
            }
        };
        workClientTimer.newTimeout(heartBeatTask, 3, TimeUnit.SECONDS);

        TimerTask jobLogUpdateTask = new TimerTask() {

            private void editLog(Job job, Exception e) {
                try {
                    HeraJobHistoryVo his = job.getJobContext().getHeraJobHistory();
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
                    HeraDebugHistoryVo history = job.getJobContext().getDebugHistory();
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
            public void run(Timeout timeout) throws Exception {
                for (Job job : new HashSet<>(workContext.getRunning().values())) {
                    try {
                        HeraJobHistoryVo history = job.getJobContext().getHeraJobHistory();
                        workContext.getJobHistoryService().update(BeanConvertUtils.convert(history));
                    } catch (Exception e) {
                        editDebugLog(job, e);
                    }
                }

                for (Job job : new HashSet<>(workContext.getManualRunning().values())) {
                    try {
                        HeraJobHistoryVo history = job.getJobContext().getHeraJobHistory();
                        workContext.getJobHistoryService().update(BeanConvertUtils.convert(history));
                    } catch (Exception e) {
                        editLog(job, e);
                    }
                }

                for (Job job : new HashSet<>(workContext.getDebugRunning().values())) {
                    try {
                        HeraDebugHistoryVo history = job.getJobContext().getDebugHistory();
                        workContext.getDebugHistoryService().updateLog(BeanConvertUtils.convert(history));
                    } catch (Exception e) {
                        editDebugLog(job, e);
                    }
                }

                workClientTimer.newTimeout(this, 5, TimeUnit.SECONDS);
            }
        };
        workClientTimer.newTimeout(jobLogUpdateTask, 5, TimeUnit.SECONDS);
    }

    /**
     * 机器启动spring-boot时，worker向主节点发起netty请求连接，成功之后，worker异步获取channel,并设置在work context中
     *
     * @param host
     * @throws Exception
     */
    public synchronized void connect(String host) throws Exception {
        if (workContext.getServerChannel() != null) {
            if (workContext.getServerHost().equals(host)) {
                return;
            } else {
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
                    log.info(workContext.getServerChannel().toString());
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

        HeraDebugHistoryVo history = job.getJobContext().getDebugHistory();
        history.setEndTime(new Date());
        history.setStatus(com.dfire.common.enums.Status.FAILED);
        workContext.getDebugHistoryService().update(BeanConvertUtils.convert(history));
        history.getLog().appendHera("任务被取消");
        workContext.getDebugHistoryService().update(BeanConvertUtils.convert(history));


    }

    public void cancelManualJob(String historyId) {
        Job job = workContext.getManualRunning().get(historyId);
        workContext.getManualRunning().remove(historyId);
        job.cancel();

        HeraJobHistoryVo history = job.getJobContext().getHeraJobHistory();
        history.setEndTime(new Date());
        String illustrate = history.getIllustrate();
        if (illustrate != null && illustrate.trim().length() > 0) {
            history.setIllustrate(illustrate + "；手动取消该任务");
        } else {
            history.setIllustrate("手动取消该任务");
        }
        history.setStatus(com.dfire.common.enums.Status.FAILED);
        workContext.getJobHistoryService().updateHeraJobHistory(BeanConvertUtils.convert(history));
        history.getLog().appendHera("任务被取消");
        workContext.getJobHistoryService().updateHeraJobHistory(BeanConvertUtils.convert(history));

    }

    public void cancelScheduleJob(String jobId) {
        Job job = workContext.getRunning().get(jobId);
        workContext.getRunning().remove(jobId);
        job.cancel();

        HeraJobHistoryVo history = job.getJobContext().getHeraJobHistory();
        history.setEndTime(new Date());
        String illustrate = history.getIllustrate();
        if (illustrate != null && illustrate.trim().length() > 0) {
            history.setIllustrate(illustrate + "；手动取消该任务");
        } else {
            history.setIllustrate("手动取消该任务");
        }
        history.setStatus(com.dfire.common.enums.Status.FAILED);
        workContext.getJobHistoryService().updateHeraJobHistory(BeanConvertUtils.convert(history));
        history.getLog().appendHera("任务被取消");
        workContext.getJobHistoryService().updateHeraJobHistory(BeanConvertUtils.convert(history));

    }


    /**
     * 页面开发中心发动执行脚本时，发起请求，
     *
     * @param kind
     * @param id
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void executeJobFromWeb(ExecuteKind kind, String id) throws ExecutionException, InterruptedException {
        WebResponse response = new WorkerHandleWebExecute().handleWebExecute(workContext, kind, id).get();
        if (response.getStatus() == Status.ERROR) {
            log.error("netty manual web request get jobStatus error");
        }
    }

    public void cancelJobFromWeb(ExecuteKind kind, String id) throws ExecutionException, InterruptedException {
        WebResponse webResponse = new WorkHandleWebCancel().handleCancel(workContext, kind, id).get();
        if (webResponse.getStatus() == Status.ERROR) {
            log.error("cancel from web exception");
        }

    }

    public void updateJobFromWeb(String jobId) throws ExecutionException, InterruptedException {
        WebResponse webResponse = new WorkHandleWebUpdate().handleUpdate(workContext, jobId).get();
        if (webResponse.getStatus() == Status.ERROR) {
            log.error("cancel from web exception");
        }

    }


}
