package com.dfire.core.netty.master.response;

import com.dfire.core.event.HeraJobMaintenanceEvent;
import com.dfire.core.event.base.Events;
import com.dfire.core.message.Protocol.*;
import com.dfire.core.netty.master.MasterContext;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午7:43 2018/5/11
 * @desc
 */
public class MasterHandleWebUpdate {

    public WebResponse handleWebUpdate(MasterContext context, WebRequest request) {
        context.getDispatcher().forwardEvent(new HeraJobMaintenanceEvent(Events.UpdateActions, request.getId()));
        WebResponse response = WebResponse.newBuilder()
                .setRid(request.getRid())
                .setOperate(WebOperate.UpdateJob)
                .setStatus(Status.OK)
                .build();
        return response;

    }
}
