package com.dfire.core.netty.worker;

import com.dfire.common.util.NamedThreadFactory;
import com.dfire.core.exception.RemotingException;
import com.dfire.core.netty.HeraChannel;
import com.dfire.core.netty.cluster.FailBackCluster;
import com.dfire.core.netty.listener.ResponseListener;
import com.dfire.core.netty.worker.request.WorkExecuteJob;
import com.dfire.core.netty.worker.request.WorkHandleCancel;
import com.dfire.core.netty.worker.request.WorkHandlerRequest;
import com.dfire.logs.ErrorLog;
import com.dfire.logs.SocketLog;
import com.dfire.logs.TaskLog;
import com.dfire.protocol.RpcRequest.Request;
import com.dfire.protocol.RpcResponse.Response;
import com.dfire.protocol.RpcSocketMessage.SocketMessage;
import com.dfire.protocol.RpcWebResponse.WebResponse;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.*;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 1:32 2018/1/4
 * @desc SocketMessage为RPC消息体
 */
public class WorkHandler extends SimpleChannelInboundHandler<SocketMessage> {


    private WorkHandlerRequest handlerRequest = new WorkHandlerRequest();

    private CompletionService<ChannelResponse> completionService = new ExecutorCompletionService<>(
            new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L,
                    TimeUnit.SECONDS,
                    new SynchronousQueue<>(),
                    new NamedThreadFactory("worker-send:", false),
                    new ThreadPoolExecutor.AbortPolicy()));

    private WorkContext workContext;

    private ConcurrentHashMap<Channel, HeraChannel> channelMap = new ConcurrentHashMap<>(2);
    private List<ResponseListener> listeners = new CopyOnWriteArrayList<>();


    public WorkHandler(final WorkContext workContext) {
        this.workContext = workContext;
        workContext.setHandler(this);
        Executor executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("send-message-to-master-thread", true));
        executor.execute(() -> {
            ChannelResponse response = null;
            Future<ChannelResponse> future;
            while (true) {
                try {
                    future = completionService.take();
                    response = future.get();
                    response.channel.writeAndFlush(wrapper(response.response));
                    TaskLog.info("1.WorkHandler: worker send response,rid={}", response.response.getRid());
                } catch (InterruptedException | ExecutionException | RemotingException e) {
                    ErrorLog.error("1.WorkHandler: worker send response timeout,rid=" + (response == null ? "" : response.response.getRid()), e);
                }
            }
        });
    }

    public void addListener(ResponseListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ResponseListener listener) {
        listeners.remove(listener);
    }

    public SocketMessage wrapper(Response response) {
        return SocketMessage
                .newBuilder()
                .setKind(SocketMessage.Kind.RESPONSE)
                .setBody(response.toByteString()).build();
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SocketMessage socketMessage) throws Exception {

        Channel channel = ctx.channel();
        switch (socketMessage.getKind()) {
            case REQUEST:
                final Request request = Request.newBuilder().mergeFrom(socketMessage.getBody()).build();
                switch (request.getOperate()) {
                    case Schedule:
                    case Manual:
                    case Debug:
                    case Rerun:
                    case SuperRecovery:
                        completionService.submit(() ->
                                new ChannelResponse(getChannel(channel), new WorkExecuteJob().execute(workContext, request).get()));
                        break;
                    case Cancel:
                        completionService.submit(() ->
                                new ChannelResponse(getChannel(channel), new WorkHandleCancel().handleCancel(workContext, request).get()));
                        break;
                    case GetWorkInfo:
                        workContext.getWorkExecuteThreadPool().execute(() -> handlerRequest.getWorkInfo(getChannel(channel)));
                        break;
                    default:
                        ErrorLog.warn("unknow operate value {}", request.getOperateValue());
                        break;
                }
            case RESPONSE:
                workContext.getWorkWebThreadPool().execute(() -> {
                    Response response = null;
                    try {
                        response = Response.newBuilder().mergeFrom(socketMessage.getBody()).build();
                    } catch (InvalidProtocolBufferException e) {
                        ErrorLog.error("解析消息异常", e);
                    }
                    TaskLog.info("4.WorkHandler:receiver: socket info from master {}, response is {}", ctx.channel().remoteAddress(), response.getRid());
                    for (ResponseListener listener : listeners) {
                        listener.onResponse(response);
                    }
                });

                break;
            case WEB_RESPONSE:
                workContext.getWorkWebThreadPool().execute(() -> {
                    WebResponse webResponse = null;
                    try {
                        webResponse = WebResponse.newBuilder().mergeFrom(socketMessage.getBody()).build();
                    } catch (InvalidProtocolBufferException e) {
                        ErrorLog.error("解析消息失败", e);
                    }
                    TaskLog.info("4.WorkHandler:receiver socket info from master {}, webResponse is {}", ctx.channel().remoteAddress(), webResponse.getRid());
                    for (ResponseListener listener : listeners) {
                        listener.onWebResponse(webResponse);
                    }
                });
                break;
            default:
                ErrorLog.error("WorkHandler:can not recognition ");
                break;

        }
    }


    private HeraChannel getChannel(Channel channel) {
        if (channelMap.get(channel) == null) {
            channelMap.putIfAbsent(channel, FailBackCluster.wrap(channel));
        }
        return channelMap.get(channel);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        SocketLog.info("客户端与服务端连接开启");
        ctx.fireChannelActive();
        getChannel(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        SocketLog.warn("客户端与服务端连接关闭");
        workContext.setServerChannel(null);
        ctx.fireChannelInactive();
        channelMap.remove(ctx.channel());
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ErrorLog.error("work exception: " + ctx.channel().remoteAddress(), cause);
    }

    private class ChannelResponse {
        HeraChannel channel;
        Response response;

        public ChannelResponse(HeraChannel channel, Response response) {
            this.channel = channel;
            this.response = response;
        }
    }

}
