package com.dfire.core.netty.master.response;

import com.dfire.common.entity.HeraDebugHistory;
import com.dfire.core.message.Protocol.*;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.core.queue.JobElement;

import java.util.Queue;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午9:48 2018/4/17
 * @desc 手动执行debug任务请求消息处理
 */
public class MasterHandleWebDebug {

    public WebResponse handleWebDebug(MasterContext context, WebRequest request) {

        String debugId = request.getId();
        Queue<JobElement> queue = context.getDebugQueue();
        for(JobElement jobElement : queue) {
            if(jobElement.getJobId().equals(debugId)) {
                WebResponse response = WebResponse.newBuilder()
                        .setRid(request.getRid())
                        .setOperate(WebOperate.ExecuteDebug)
                        .setStatus(Status.ERROR)
                        .setErrorText("任务已经在队列中")
                        .build();
                return response;
            }
        }
        HeraDebugHistory debugHistory = context.getHeraDebugHistoryService().findDebugHistoryById(debugId);
        context.getMaster().debug(debugHistory);

        WebResponse response = WebResponse.newBuilder()
                .setRid(request.getRid())
                .setOperate(WebOperate.ExecuteDebug)
                .setStatus(Status.OK)
                .build();
        return response;
    }


}
