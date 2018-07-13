package com.dfire.core.netty.master.response;

import com.dfire.core.message.Protocol;
import com.dfire.core.netty.master.MasterContext;

/**
 *
 * @author xiaosuda
 * @date 2018/7/12
 */
public class MasterGenerateAction {

    public Protocol.WebResponse generateActionByJobId(MasterContext context, Protocol.WebRequest request) {

         boolean result = context.getMaster().generateSingleAction(Integer.parseInt(request.getId()));

        return Protocol.WebResponse.newBuilder()
                .setRid(request.getRid())
                .setOperate(Protocol.WebOperate.ExecuteJob)
                .setStatus(result ? Protocol.Status.OK : Protocol.Status.ERROR)
                .build();

    }
}
