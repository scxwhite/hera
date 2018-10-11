package com.dfire.core.netty.master.response;

import com.dfire.core.netty.master.MasterContext;
import com.dfire.protocol.ResponseStatus;
import com.dfire.protocol.RpcWebOperate;
import com.dfire.protocol.RpcWebRequest;
import com.dfire.protocol.RpcWebResponse;

/**
 *
 * @author xiaosuda
 * @date 2018/7/12
 */
public class MasterGenerateAction {

    public RpcWebResponse.WebResponse generateActionByJobId(MasterContext context, RpcWebRequest.WebRequest request) {

         boolean result = context.getMaster().generateSingleAction(Integer.parseInt(request.getId()));

        return RpcWebResponse.WebResponse.newBuilder()
                .setRid(request.getRid())
                .setOperate(RpcWebOperate.WebOperate.ExecuteJob)
                .setStatus(result ? ResponseStatus.Status.OK : ResponseStatus.Status.ERROR)
                .build();

    }
}
