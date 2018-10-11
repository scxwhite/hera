package com.dfire.core.netty.master.response;

import com.dfire.core.event.HeraJobMaintenanceEvent;
import com.dfire.core.event.base.Events;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.protocol.ResponseStatus;
import com.dfire.protocol.RpcWebOperate;
import com.dfire.protocol.RpcWebRequest;
import com.dfire.protocol.RpcWebResponse.*;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午7:43 2018/5/11
 * @desc
 */
public class MasterHandleWebUpdate {

    public WebResponse handleWebUpdate(MasterContext context, RpcWebRequest.WebRequest request) {
        String id = request.getId();
        context.getMaster().generateSingleAction(Integer.parseInt(id));
        context.getDispatcher().forwardEvent(new HeraJobMaintenanceEvent(Events.UpdateJob, id));
        return WebResponse.newBuilder()
                .setRid(request.getRid())
                .setOperate(RpcWebOperate.WebOperate.UpdateJob)
                .setStatus(ResponseStatus.Status.OK)
                .build();

    }
}
