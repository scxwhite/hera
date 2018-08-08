package com.dfire.core.netty.master;

import com.dfire.common.service.EmailService;
import com.dfire.core.message.Protocol.*;
import com.dfire.core.netty.listener.ResponseListener;
import com.dfire.core.netty.master.response.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 1:34 2018/1/4
 * @desc SocketMessage为rpc消息体
 */
@Slf4j
@ChannelHandler.Sharable
public class MasterHandler extends ChannelInboundHandlerAdapter {

    private CompletionService<ChannelResponse> completionService = new ExecutorCompletionService<>(Executors.newCachedThreadPool());

    /**
     * 调度器执行上下文信息
     */
    private MasterContext masterContext;

    /**
     * 开发中心执行任务时候，masterHandler在read到SocketMessage处理逻辑
     */
    private MasterHandleWebDebug masterHandleWebDebug = new MasterHandleWebDebug();

    /**
     * 主节点接收到心跳，masterHandler在read到HeartBeat处理逻辑
     */
    private MasterHandleHeartBeat masterDoHeartBeat = new MasterHandleHeartBeat();

    /**
     * master接受到worker取消执行任务请求的处理逻辑
     */
    private MasterHandleWebCancel masterHandleCancelJob = new MasterHandleWebCancel();

    /**
     * 调度中心执行任务时候，masterHandler在read到SocketMessage的任务处理消息的时候的处理逻辑
     */
    private MasterHandleWebExecute masterHandleWebExecute = new MasterHandleWebExecute();

    /**
     * 生成单个任务action
     */
    private MasterGenerateAction masterGenerateAction = new MasterGenerateAction();

    private MasterHandleWebUpdate masterHandleWebUpdate = new MasterHandleWebUpdate();


    public MasterHandler(MasterContext masterContext) {
        this.masterContext = masterContext;
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
                    while (true) {
                        try {
                            Future<ChannelResponse> future = completionService.take();
                            ChannelResponse response = future.get();
                            response.channel.writeAndFlush(wrapper(response.webResponse));
                            log.info("master send response success");
                        } catch (Exception e) {
                            log.error("master handler future take error");
                            throw new RuntimeException(e);
                        }
                    }
                }
        );
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        SocketMessage socketMessage = (SocketMessage) msg;
        Channel channel = ctx.channel();
        switch (socketMessage.getKind()) {
            //心跳
            case REQUEST:
                Request request = Request.newBuilder().mergeFrom(socketMessage.getBody()).build();
                if (request.getOperate() == Operate.HeartBeat) {
                    masterDoHeartBeat.handleHeartBeat(masterContext, channel, request);
                }
                break;
            case WEB_REQUEST:
                final WebRequest webRequest = WebRequest.newBuilder().mergeFrom(socketMessage.getBody()).build();
                switch (webRequest.getOperate()) {
                    case ExecuteJob:
                        completionService.submit(() ->
                                new ChannelResponse(channel, masterHandleWebExecute.handleWebExecute(masterContext, webRequest)));
                        break;
                    case CancelJob:
                        completionService.submit(() ->
                                new ChannelResponse(channel, masterHandleCancelJob.handleWebCancel(masterContext, webRequest)));
                        break;
                    case UpdateJob:
                        completionService.submit(() ->
                                new ChannelResponse(channel, masterHandleWebUpdate.handleWebUpdate(masterContext, webRequest)));
                        break;
                    case ExecuteDebug:
                        completionService.submit(() ->
                                new ChannelResponse(channel, masterHandleWebDebug.handleWebDebug(masterContext, webRequest)));
                        break;
                    case GenerateAction:
                        completionService.submit(() ->
                                new ChannelResponse(channel, masterGenerateAction.generateActionByJobId(masterContext, webRequest)));
                        break;
                    default:
                        log.error("unknown operate error:{}", webRequest.getOperate());
                        break;
                }
                break;
            case RESPONSE:
                for (ResponseListener listener : listeners) {
                    listener.onResponse(Response.newBuilder().mergeFrom(socketMessage.getBody()).build());
                }
                break;
            case WEB_RESPONSE:
                for (ResponseListener listener : listeners) {
                    listener.onWebResponse(WebResponse.newBuilder().mergeFrom(socketMessage.getBody()).build());
                }
                break;
            default:
                log.error("unknown request type : {}", socketMessage.getKind());
                break;
        }

        super.channelRead(ctx, msg);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        masterContext.getWorkMap().put(channel, new MasterWorkHolder(ctx.channel()));
        SocketAddress remoteAddress = channel.remoteAddress();
        log.info("worker client channel registered connect success : {}", remoteAddress.toString());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        log.info("worker miss connection !!!");

        //TODO 解决work正在执行任务，却无法回写任务状态 or work宕机 任务状态消失
        MasterWorkHolder workHolder = masterContext.getWorkMap().get(ctx.channel());
        EmailService emailService = masterContext.getEmailService();

        StringBuilder content = new StringBuilder();
        content.append("不幸的消息，work宕机了:").append(ctx.channel().remoteAddress()).append("<br>");
        content.append("自动调度队列任务：").append(workHolder.getHeartBeatInfo().getRunning()).append("<br>");
        content.append("手动队列任务：").append(workHolder.getHeartBeatInfo().getManualRunning()).append("<br>");
        content.append("开发中心队列任务：").append(workHolder.getHeartBeatInfo().getDebugRunning()).append("<br>");
        emailService.sendEmail("work断开连接：", content.toString(), new String[] {
             "xiaosuda@2dfire.com"
        });

        // work断开  不再分发任务
        masterContext.getWorkMap().remove(ctx.channel());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        SocketAddress remoteAddress = channel.remoteAddress();
        log.info("worker client channel active success : {}", remoteAddress.toString());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    private SocketMessage wrapper(WebResponse response) {
        return SocketMessage.newBuilder().setKind(SocketMessage.Kind.WEB_RESPONSE).setBody(response.toByteString()).build();
    }

    private List<ResponseListener> listeners = new CopyOnWriteArrayList<>();

    public void addListener(ResponseListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ResponseListener listener) {
        listeners.remove(listener);
    }


    private class ChannelResponse {
        Channel channel;
        WebResponse webResponse;

        public ChannelResponse(Channel channel, WebResponse webResponse) {
            this.channel = channel;
            this.webResponse = webResponse;
        }
    }
}
