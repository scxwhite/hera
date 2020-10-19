package com.dfire.core.route.loadbalance.impl;

import com.dfire.common.entity.vo.HeraHostGroupVo;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.core.netty.master.MasterWorkHolder;
import com.dfire.core.route.loadbalance.AbstractLoadBalance;
import com.dfire.logs.ScheduleLog;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机算法
 * @author xiaosdua
 */
public class RandomLoadBalance extends AbstractLoadBalance {

    public static final String NAME = "random";

    @Override
    protected MasterWorkHolder doSelect(HeraHostGroupVo hostGroup, MasterContext masterContext) {
        List<String> hosts = hostGroup.getHosts();
        int size = hosts.size();
        Set<Integer> hasCheck = new HashSet<>(size);
        int select;
        for (int i = 0; i < size; i++) {
            for (; ; ) {
                select = ThreadLocalRandom.current().nextInt(size);
                if (!hasCheck.contains(select)) {
                    hasCheck.add(select);
                    break;
                }
            }
            for (MasterWorkHolder workHolder : masterContext.getWorkMap().values()) {
                if (workHolder.getHeartBeatInfo() != null && workHolder.getHeartBeatInfo().getHost().equals(hosts.get(select).trim())) {
                    if (check(workHolder)) {
                        ScheduleLog.warn("select work is :{}", workHolder.getChannel().getRemoteAddress());
                        return workHolder;
                    }
                    break;
                }
            }
        }
        return null;
    }
}
