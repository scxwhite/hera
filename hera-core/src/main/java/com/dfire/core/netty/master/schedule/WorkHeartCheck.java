package com.dfire.core.netty.master.schedule;

import com.dfire.core.netty.ScheduledChore;
import com.dfire.core.netty.master.Master;
import com.dfire.core.netty.master.MasterWorkHolder;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * desc: work心跳超时check
 *
 * @author scx
 * @create 2020/01/06
 */
public class WorkHeartCheck extends ScheduledChore {


    private Master master;

    private WorkHeartCheck(Master master, long initialDelay, long period, TimeUnit unit) {
        super("WorkHeartCheck", initialDelay, period, unit);
        this.master = master;
    }

    public WorkHeartCheck(Master master) {
        this(master, 1, 1, TimeUnit.MINUTES);
    }

    @Override
    protected void chore() {
        Date now = new Date();
        Map<Channel, MasterWorkHolder> workMap = master.getMasterContext().getWorkMap();
        List<Channel> removeChannel = new ArrayList<>(workMap.size());
        for (Channel channel : workMap.keySet()) {
            MasterWorkHolder workHolder = workMap.get(channel);
            if (workHolder.getHeartBeatInfo() == null) {
                continue;
            }
            Long workTime = workHolder.getHeartBeatInfo().getTimestamp();
            if (workTime == null || now.getTime() - workTime > 1000 * 60L) {
                workHolder.getChannel().close();
                removeChannel.add(channel);
            }
        }
        removeChannel.forEach(workMap::remove);
    }
}
