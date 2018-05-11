package com.dfire.core.netty.master.response;

import com.dfire.common.entity.HeraDebugHistory;
import com.dfire.common.entity.HeraJobHistory;
import com.dfire.core.message.Protocol.*;
import com.dfire.core.netty.master.MasterContext;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午7:20 2018/5/11
 * @desc
 */
@Slf4j
public class MasterHandleWebExecute {

    public WebResponse handleWebExecute(MasterContext context, WebRequest request) {
        if(request.getEk() == ExecuteKind.ManualKind || request.getEk() == ExecuteKind.ScheduleKind) {
            String historyId = request.getId();
            HeraJobHistory history = context.getHeraJobHistoryService().findJobHistory(historyId);
            String jobId = history.getJobId();
            context.getMaster().run(history);
            WebResponse webResponse = WebResponse.newBuilder()
                    .setRid(request.getRid())
                    .setOperate(WebOperate.ExecuteJob)
                    .setStatus(Status.OK)
                    .build();
            log.info("send web execute response, jobId = " + jobId);
            return webResponse;
        } else if(request.getEk() == ExecuteKind.DebugKind) {
            String debugId = request.getId();
            HeraDebugHistory debugHistory = context.getHeraDebugHistoryService().findDebugHistoryById(debugId);
            log.info("receive web debug response, debugId = " + debugId);
            context.getMaster().debug(debugHistory);

            WebResponse webResponse = WebResponse.newBuilder()
                    .setRid(request.getRid())
                    .setOperate(WebOperate.ExecuteDebug)
                    .setStatus(Status.OK)
                    .build();
            log.info("send web debug response, debugId = " + debugId);
            return webResponse;
        }
        return null;
    }

}
