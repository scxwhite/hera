package com.dfire.core.netty.master;

import com.dfire.common.util.NamedThreadFactory;
import com.dfire.core.netty.HeraChannel;
import com.dfire.core.netty.NettyChannel;
import com.dfire.core.netty.listener.ResponseListener;
import com.dfire.core.netty.master.response.MasterHandleHeartBeat;
import com.dfire.core.netty.master.response.MasterHandlerWebResponse;
import com.dfire.logs.SocketLog;
import com.dfire.logs.TaskLog;
import com.dfire.protocol.RpcOperate.Operate;
import com.dfire.protocol.RpcRequest.Request;
import com.dfire.protocol.RpcResponse.Response;
import com.dfire.protocol.RpcSocketMessage.SocketMessage;
import com.dfire.protocol.RpcWebRequest.WebRequest;
import com.dfire.protocol.RpcWebResponse.WebResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 1:34 2018/1/4
 * @desc SocketMessage为rpc消息体
 */
@ChannelHandler.Sharable
public class MasterHandler extends ChannelInboundHandlerAdapter {

    private CompletionService<ChannelResponse> completionService;

    /**
     * 调度器执行上下文信息
     */
    private MasterContext masterContext;


    /**
     * 主节点接收到心跳，masterHandler在read到HeartBeat处理逻辑
     */
    private MasterHandleHeartBeat masterDoHeartBeat = new MasterHandleHeartBeat();


    public MasterHandler(MasterContext masterContext) {
        this.masterContext = masterContext;
        completionService = new ExecutorCompletionService<>(
                new ThreadPoolExecutor(
                        0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), new NamedThreadFactory("master-execute", false), new ThreadPoolExecutor.AbortPolicy()));
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new NamedThreadFactory("master-deal", false), new ThreadPoolExecutor.AbortPolicy());
        executor.execute(() -> {
                    Future<ChannelResponse> future;
                    ChannelResponse response;
                    while (true) {
                        try {
                            future = completionService.take();
                            response = future.get();
                            TaskLog.info("3-1.MasterHandler:-->master prepare send status : {}", response.webResponse.getStatus());
                            response.channel.writeAndFlush(wrapper(response.webResponse));
                            TaskLog.info("3-2.MasterHandler:-->master send response success, requestId={}", response.webResponse.getRid());
                        } catch (Exception e) {
                            SocketLog.error("master handler future take error:{}", e);
                            e.printStackTrace();
                        } catch (Throwable throwable) {
                            SocketLog.error("master handler future take throwable{}", throwable);
                            throwable.printStackTrace();
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
                                new ChannelResponse(new NettyChannel(channel), MasterHandlerWebResponse.handleWebExecute(masterContext, webRequest)));
                        break;
                    case CancelJob:
                        completionService.submit(() ->
                                new ChannelResponse(new NettyChannel(channel), MasterHandlerWebResponse.handleWebCancel(masterContext, webRequest)));
                        break;
                    case UpdateJob:
                        completionService.submit(() ->
                                new ChannelResponse(new NettyChannel(channel), MasterHandlerWebResponse.handleWebUpdate(masterContext, webRequest)));
                        break;
                    case ExecuteDebug:
                        completionService.submit(() ->
                                new ChannelResponse(new NettyChannel(channel), MasterHandlerWebResponse.handleWebDebug(masterContext, webRequest)));
                        break;
                    case GenerateAction:
                        completionService.submit(() ->
                                new ChannelResponse(new NettyChannel(channel), MasterHandlerWebResponse.generateActionByJobId(masterContext, webRequest)));
                        break;

                    case GetAllHeartBeatInfo:
                        completionService.submit(() ->
                                new ChannelResponse(new NettyChannel(channel), MasterHandlerWebResponse.buildJobQueueInfo(masterContext, webRequest)));
                        break;
                    default:
                        SocketLog.error("unknown operate error:{}", webRequest.getOperate());
                        break;
                }
                break;
            case RESPONSE:
                Response response = Response.newBuilder().mergeFrom(socketMessage.getBody()).build();
                SocketLog.info("6.MasterHandler:receiver socket info from work {}, response is {}", ctx.channel().remoteAddress(), response.getRid());
                for (ResponseListener listener : listeners) {
                    listener.onResponse(response);
                }
                break;
            case WEB_RESPONSE:
                WebResponse webResponse = WebResponse.newBuilder().mergeFrom(socketMessage.getBody()).build();
                SocketLog.info("6.MasterHandler:receiver socket info from work {}, webResponse is {}", ctx.channel().remoteAddress(), webResponse.getRid());
                for (ResponseListener listener : listeners) {
                    listener.onWebResponse(webResponse);
                }
                break;
            default:
                SocketLog.error("unknown request type : {}", socketMessage.getKind());
                break;
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        masterContext.getWorkMap().put(channel, new MasterWorkHolder(new NettyChannel(ctx.channel())));
        SocketAddress remoteAddress = channel.remoteAddress();
        SocketLog.info("worker client channel registered connect success : {}", remoteAddress.toString());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        SocketLog.error("worker miss connection !!!");
        masterContext.getMaster().workerDisconnectProcess(ctx.channel());
        super.channelUnregistered(ctx);

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        SocketAddress remoteAddress = channel.remoteAddress();
        SocketLog.info("worker client channel active success : {}", remoteAddress.toString());
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
        HeraChannel channel;
        WebResponse webResponse;

        public ChannelResponse(HeraChannel channel, WebResponse webResponse) {
            this.channel = channel;
            this.webResponse = webResponse;
        }
    }
}
