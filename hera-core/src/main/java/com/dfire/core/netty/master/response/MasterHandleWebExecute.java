package com.dfire.core.netty.master.response;

import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.entity.vo.HeraDebugHistoryVo;
import com.dfire.common.entity.vo.HeraJobHistoryVo;
import com.dfire.common.util.BeanConvertUtils;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.logs.SocketLog;
import com.dfire.protocol.JobExecuteKind;
import com.dfire.protocol.ResponseStatus;
import com.dfire.protocol.RpcWebOperate;
import com.dfire.protocol.RpcWebRequest;
import com.dfire.protocol.RpcWebResponse.WebResponse;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午7:20 2018/5/11
 * @desc
 */
public class MasterHandleWebExecute {

    public WebResponse handleWebExecute(MasterContext context, RpcWebRequest.WebRequest request) {
        if (request.getEk() == JobExecuteKind.ExecuteKind.ManualKind || request.getEk() == JobExecuteKind.ExecuteKind.ScheduleKind) {
            String historyId = request.getId();

            HeraJobHistory heraJobHistory = context.getHeraJobHistoryService().findById(historyId);
            HeraJobHistoryVo history = BeanConvertUtils.convert(heraJobHistory);
            String jobId = history.getJobId();
            context.getMaster().run(history);
            WebResponse webResponse = WebResponse.newBuilder()
                    .setRid(request.getRid())
                    .setOperate(RpcWebOperate.WebOperate.ExecuteJob)
                    .setStatus(ResponseStatus.Status.OK)
                    .build();
            SocketLog.info("send web execute response, actionId = {} ", jobId);
            return webResponse;
        } else if (request.getEk() == JobExecuteKind.ExecuteKind.DebugKind) {
            String debugId = request.getId();
            HeraDebugHistoryVo debugHistory = context.getHeraDebugHistoryService().findById(debugId);
            SocketLog.info("receive web debug response, debugId = " + debugId);
            context.getMaster().debug(debugHistory);

            WebResponse webResponse = WebResponse.newBuilder()
                    .setRid(request.getRid())
                    .setOperate(RpcWebOperate.WebOperate.ExecuteJob)
                    .setStatus(ResponseStatus.Status.OK)
                    .build();
            SocketLog.info("send web debug response, debugId = {}", debugId);
            return webResponse;
        }
        return WebResponse.newBuilder()
                .setRid(request.getRid())
                .setErrorText("未识别的操作类型" + request.getEk())
                .setStatus(ResponseStatus.Status.ERROR)
                .build();
    }

}
