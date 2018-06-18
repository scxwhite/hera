package com.dfire.core.netty.master.response;

import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.entity.vo.HeraJobHistoryVo;
import com.dfire.common.util.BeanConvertUtils;
import com.dfire.core.message.Protocol.*;
import com.dfire.core.netty.listener.ResponseListener;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.core.netty.master.MasterWorkHolder;
import com.dfire.core.netty.util.AtomicIncrease;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午2:26 2018/4/25
 * @desc master向worker发送执行job的指令
 */
@Slf4j
public class MasterExecuteJob {

    public Future<Response> executeJob(final MasterContext context, final MasterWorkHolder holder, ExecuteKind kind, final String id) {
        if (kind == ExecuteKind.DebugKind) {
            return executeDebugJob(context, holder, id);
        } else if (kind == ExecuteKind.ScheduleKind) {
            executeScheduleJob(context, holder, id);
        } else if (kind == ExecuteKind.ManualKind) {
            executeManualJob(context, holder, id);
        }
        return null;
    }

    /**
     * @param [context, holder, id]
     * @return java.util.concurrent.Future<com.dfire.core.message.Protocol.Response>
     * @desc 向channel发送执行job命令，等待worker响应，响应ok,则添加监听器，继续等待任务完成消息，响应失败，返回失败退出码
     */
    private Future<Response> executeManualJob(MasterContext context, MasterWorkHolder holder, String jobId) {
        holder.getManningRunning().put(jobId, false);

        ExecuteMessage message = ExecuteMessage.newBuilder().setJobId(jobId).build();
        final Request request = Request.newBuilder()
                .setRid(AtomicIncrease.getAndIncrement())
                .setOperate(Operate.Manual)
                .setBody(message.toByteString())
                .build();
        SocketMessage socketMessage = SocketMessage.newBuilder()
                .setKind(SocketMessage.Kind.REQUEST)
                .setBody(request.toByteString())
                .build();
        Future<Response> future = context.getThreadPool().submit(new Callable<Response>() {
            private Response result;

            @Override
            public Response call() throws Exception {
                final CountDownLatch latch = new CountDownLatch(1);
                context.getHandler().addListener(new ResponseListener() {
                    @Override
                    public void onWebResponse(WebResponse webResponse) {
                    }

                    @Override
                    public void onResponse(Response response) {
                        if (response.getRid() == request.getRid()) {
                            context.getHandler().removeListener(this);
                            result = response;
                            latch.countDown();
                        }

                    }
                });
                latch.await();
                holder.getRunning().remove(jobId);
                return result;
            }
        });
        holder.getChannel().writeAndFlush(socketMessage);
        return future;
    }

    /**
     * @param [context, holder, id]
     * @return void
     * @desc 向channel发送执行job命令，等待worker响应，响应ok,则添加监听器，继续等待任务完成消息，响应失败，返回失败退出码
     */
    private Future<Response> executeScheduleJob(MasterContext context, MasterWorkHolder holder, String id) {

        HeraJobHistory heraJobHistory = context.getHeraJobHistoryService().findById(id);
        HeraJobHistoryVo history = BeanConvertUtils.convert(heraJobHistory);
        final String jobId = history.getJobId();
        holder.getRunning().put(jobId, false);

        ExecuteMessage message = ExecuteMessage.newBuilder().setJobId(jobId).build();
        final Request request = Request.newBuilder()
                .setRid(AtomicIncrease.getAndIncrement())
                .setOperate(Operate.Schedule)
                .setBody(message.toByteString())
                .build();
        SocketMessage socketMessage = SocketMessage.newBuilder()
                .setKind(SocketMessage.Kind.REQUEST)
                .setBody(request.toByteString())
                .build();
        Future<Response> future = context.getThreadPool().submit(new Callable<Response>() {
            private Response result;

            @Override
            public Response call() throws Exception {
                final CountDownLatch latch = new CountDownLatch(1);
                context.getHandler().addListener(new ResponseListener() {
                    @Override
                    public void onResponse(Response response) {
                        if (response.getRid() == request.getRid()) {
                            context.getHandler().removeListener(this);
                            result = response;
                            latch.countDown();
                        }

                    }

                    @Override
                    public void onWebResponse(WebResponse webResponse) {
                    }
                });
                latch.await();
                holder.getRunning().remove(jobId);
                return result;
            }
        });
        holder.getChannel().writeAndFlush(socketMessage);
        return future;

    }


    private Future<Response> executeDebugJob(MasterContext context, MasterWorkHolder holder, String id) {
        holder.getDebugRunning().put(id, false);
        DebugMessage message = DebugMessage
                .newBuilder()
                .setDebugId(id)
                .build();
        final Request request = Request.newBuilder()
                .setRid(AtomicIncrease.getAndIncrement())
                .setOperate(Operate.Debug)
                .setBody(message.toByteString())
                .build();
        SocketMessage socketMessage = SocketMessage
                .newBuilder()
                .setKind(SocketMessage.Kind.REQUEST)
                .setBody(request.toByteString())
                .build();
        Future<Response> future = context.getThreadPool().submit(new Callable<Response>() {
            private Response response;

            @Override
            public Response call() throws Exception {
                final CountDownLatch latch = new CountDownLatch(1);
                context.getHandler().addListener(new ResponseListener() {
                    @Override
                    public void onResponse(Response resp) {
                        if (resp.getRid() == request.getRid()) {
                            context.getHandler().removeListener(this);
                            response = resp;
                            latch.countDown();
                        }
                    }
                    @Override
                    public void onWebResponse(WebResponse resp) { }
                });
                try {
                    latch.await();
                } finally {
                    holder.getDebugRunning().remove(id);
                }
                return response;
            }
        });

        /**
         * writeAndFlush 和 write有和区别，为何使用write workerHandler无法接收数据
         */
        holder.getChannel().writeAndFlush(socketMessage);
        log.info("master send debug command to worker,rid=" + request.getRid() + ",debugId=" + id);
        return future;
    }
}
