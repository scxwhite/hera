package com.dfire.core.netty.master.response;

import com.dfire.common.entity.vo.HeraDebugHistoryVo;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.core.queue.JobElement;
import com.dfire.protocol.ResponseStatus;
import com.dfire.protocol.RpcWebOperate;
import com.dfire.protocol.RpcWebRequest;
import com.dfire.protocol.RpcWebResponse.*;

import java.util.Queue;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午9:48 2018/4/17
 * @desc 手动执行debug任务请求消息处理
 */
public class MasterHandleWebDebug {

    public WebResponse handleWebDebug(MasterContext context, RpcWebRequest.WebRequest request) {

        String debugId = request.getId();
        Queue<JobElement> queue = context.getDebugQueue();
        WebResponse response;
        for (JobElement jobElement : queue) {
            if (jobElement.getJobId().equals(debugId)) {
                response = WebResponse.newBuilder()
                        .setRid(request.getRid())
                        .setOperate(RpcWebOperate.WebOperate.ExecuteDebug)
                        .setStatus(ResponseStatus.Status.ERROR)
                        .setErrorText("任务已经在队列中")
                        .build();
                return response;
            }
        }
        HeraDebugHistoryVo debugHistory = context.getHeraDebugHistoryService().findById(debugId);
        context.getMaster().debug(debugHistory);
        return WebResponse.newBuilder()
                .setRid(request.getRid())
                .setOperate(RpcWebOperate.WebOperate.ExecuteDebug)
                .setStatus(ResponseStatus.Status.OK)
                .build();
    }


}
