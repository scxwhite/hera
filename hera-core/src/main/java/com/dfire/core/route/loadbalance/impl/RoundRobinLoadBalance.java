package com.dfire.core.route.loadbalance.impl;

import com.dfire.common.entity.vo.HeraHostGroupVo;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.core.netty.master.MasterWorkHolder;
import com.dfire.core.route.loadbalance.AbstractLoadBalance;
import com.dfire.logs.ScheduleLog;

import java.util.List;

/**
 * 轮询
 *
 * @author wyr
 */
public class RoundRobinLoadBalance extends AbstractLoadBalance {


    public static final String NAME = "roundrobin";

    @Override
    protected MasterWorkHolder doSelect(HeraHostGroupVo hostGroup, MasterContext masterContext) {
        List<String> hosts = hostGroup.getHosts();
        int index = hostGroup.getNextPos();
        int size = hosts.size();
        for (int i = 0; i < size; i++) {
            String host = hosts.get(index);
            for (MasterWorkHolder workHolder : masterContext.getWorkMap().values()) {
                if (workHolder.getHeartBeatInfo() != null && workHolder.getHeartBeatInfo().getHost().equals(host.trim())) {
                    if (check(workHolder)) {
                        hostGroup.setNextPos(++index >= size ? 0 : index);
                        ScheduleLog.warn("select work is :{}", workHolder.getChannel().getRemoteAddress());
                        return workHolder;
                    }
                    break;
                }
            }
            if (++index >= size) {
                index = 0;
            }
        }
        return null;
    }

}
