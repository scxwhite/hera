package com.dfire.core.netty.worker;

import com.dfire.common.util.NamedThreadFactory;
import com.dfire.core.exception.RemotingException;
import com.dfire.core.netty.listener.ResponseListener;
import com.dfire.core.netty.worker.request.WorkExecuteJob;
import com.dfire.core.netty.worker.request.WorkHandleCancel;
import com.dfire.core.netty.worker.request.WorkHandlerRequest;
import com.dfire.logs.SocketLog;
import com.dfire.logs.TaskLog;
import com.dfire.protocol.RpcRequest.Request;
import com.dfire.protocol.RpcResponse.Response;
import com.dfire.protocol.RpcSocketMessage.SocketMessage;
import com.dfire.protocol.RpcWebResponse.WebResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;
import java.util.concurrent.*;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 1:32 2018/1/4
 * @desc SocketMessage为RPC消息体
 */
public class WorkHandler extends SimpleChannelInboundHandler<SocketMessage> {


    private WorkHandlerRequest handlerRequest = new WorkHandlerRequest();

    private CompletionService<Response> completionService = new ExecutorCompletionService<>(
            new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L,
                    TimeUnit.SECONDS,
                    new SynchronousQueue<>(),
                    new NamedThreadFactory("worker-send:", false),
                    new ThreadPoolExecutor.AbortPolicy()));

    private WorkContext workContext;

    public WorkHandler(final WorkContext workContext) {
        this.workContext = workContext;
        workContext.setHandler(this);
        Executor executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            Response response = null;
            Future<Response> future;
            while (true) {
                try {
                    future = completionService.take();
                    response = future.get();
                    workContext.getServerChannel().writeAndFlush(wrapper(response));
                    TaskLog.info("1.WorkHandler: worker send response,rid={}", response.getRid());
                } catch (InterruptedException | ExecutionException | RemotingException e) {
                    e.printStackTrace();
                    TaskLog.error("1.WorkHandler: worker send response timeout,rid={}", response == null ? null : response.getRid());
                }
            }
        });
    }


    private List<ResponseListener> listeners = new CopyOnWriteArrayList<>();

    public void addListener(ResponseListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ResponseListener listener) {
        listeners.add(listener);
    }

    public SocketMessage wrapper(Response response) {
        return SocketMessage
                .newBuilder()
                .setKind(SocketMessage.Kind.RESPONSE)
                .setBody(response.toByteString()).build();
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SocketMessage socketMessage) throws Exception {
        switch (socketMessage.getKind()) {
            case REQUEST:
                final Request request = Request.newBuilder().mergeFrom(socketMessage.getBody()).build();
                switch (request.getOperate()) {
                    case Schedule:
                    case Manual:
                    case Debug:
                        completionService.submit(() ->
                                new WorkExecuteJob().execute(workContext, request).get());
                        break;
                    case Cancel:
                        completionService.submit(() ->
                                new WorkHandleCancel().handleCancel(workContext, request).get());
                        break;
                    case GetWorkInfo:
                        handlerRequest.getWorkInfo(ctx.channel());
                        break;
                    default:
                        SocketLog.error("unknow operate value {}", request.getOperateValue());
                        break;
                }
            case RESPONSE:
                final Response response = Response.newBuilder().mergeFrom(socketMessage.getBody()).build();
                TaskLog.info("4.WorkHandler:receiver: socket info from master {}, response is {}", ctx.channel().remoteAddress(), response.getRid());
                for (ResponseListener listener : listeners) {
                    listener.onResponse(response);
                }
                break;
            case WEB_RESPONSE:
                WebResponse webResponse = WebResponse.newBuilder().mergeFrom(socketMessage.getBody()).build();
                TaskLog.info("4.WorkHandler:receiver socket info from master {}, webResponse is {}", ctx.channel().remoteAddress(), webResponse.getRid());
                for (ResponseListener listener : listeners) {
                    listener.onWebResponse(webResponse);
                }
                break;
            default:
                SocketLog.error("WorkHandler:can not recognition ");
                break;

        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        SocketLog.info("客户端与服务端连接开启");
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        SocketLog.warn("客户端与服务端连接关闭");
        workContext.setServerChannel(null);
        ctx.fireChannelInactive();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        SocketLog.error("work exception: {}, {}", ctx.channel().remoteAddress(), cause.toString());
        super.exceptionCaught(ctx, cause);
    }

}
