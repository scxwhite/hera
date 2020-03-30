package com.dfire.core.netty;

import com.dfire.common.util.NamedThreadFactory;
import com.dfire.logs.ErrorLog;
import com.dfire.logs.HeraLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

/**
 * desc:
 *
 * @author scx
 * @create 2020/01/06
 */
public class ChoreService implements ChoreServicer {

    private final ScheduledThreadPoolExecutor scheduler;

    private final Integer MIN_CORE_POOL_SIZE = 1;

    private final Map<ScheduledChore, ScheduledFuture<?>> scheduledChores;


    public ChoreService(Integer coreSize) {
        this(coreSize, new NamedThreadFactory("chore-service", true));
    }

    public ChoreService(Integer coreSize, String factoryName) {
        this(coreSize, new NamedThreadFactory(factoryName, true));
    }

    public ChoreService(Integer coreSize, ThreadFactory factory) {
        if (coreSize < MIN_CORE_POOL_SIZE) {
            coreSize = MIN_CORE_POOL_SIZE;
        }
        scheduler = new ScheduledThreadPoolExecutor(coreSize, factory);
        scheduler.setRemoveOnCancelPolicy(true);
        scheduledChores = new ConcurrentHashMap<>();
    }


    @Override
    public void cancelChore(ScheduledChore chore) {
        cancelChore(chore, true);
    }

    @Override
    public void cancelChore(ScheduledChore chore, boolean mayInterruptIfRunning) {
        ScheduledFuture<?> future;
        if (chore != null && (future = scheduledChores.get(chore)) != null) {
            future.cancel(mayInterruptIfRunning);
            scheduledChores.remove(chore);
            HeraLog.info("cancel {} chore schedule success", chore.getName());

        }
    }

    @Override
    public boolean isChoreScheduled(ScheduledChore chore) {
        return chore != null && scheduledChores.containsKey(chore) && !scheduledChores.get(chore).isDone();
    }

    @Override
    public boolean triggerNow(ScheduledChore chore) {
        if (chore == null) {
            return false;
        }
        reScheduleChore(chore);
        return true;
    }

    @Override
    public void shutDown() {
        HeraLog.info("shutdown chore service");
        scheduler.shutdownNow();
        cancelAllChores(true);
        scheduledChores.clear();
    }

    private void cancelAllChores(boolean mayInterruptIfRunning) {
        List<ScheduledChore> scheduledChoreList = new ArrayList<>(scheduledChores.size());
        scheduledChoreList.addAll(scheduledChores.keySet());
        for (ScheduledChore scheduledChore : scheduledChoreList) {
            this.cancelChore(scheduledChore, mayInterruptIfRunning);
        }
    }

    private void reScheduleChore(ScheduledChore chore) {
        if (chore == null) {
            return;
        }
        ScheduledFuture<?> future = scheduledChores.get(chore);
        if (future != null && !future.isDone()) {
            future.cancel(false);
        }
        scheduledChore(chore);
    }

    /**
     * 以rate方式调度
     *
     * @param chore
     * @return
     */

    public boolean scheduledChore(ScheduledChore chore) {
        if (chore == null) {
            return false;
        }
        try {
            chore.setChoreService(this);
            ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(chore, chore.getInitialDelay(), chore.getPeriod(), chore.getUnit());
            scheduledChores.put(chore, future);
            HeraLog.info("start {} chore schedule success", chore.getName());
            return true;
        } catch (Exception e) {
            ErrorLog.error("Could not successfully schedule chore ", e);
        }
        return false;
    }

    /**
     * 只调度一次
     *
     * @param chore
     * @return
     */
    public boolean scheduledChoreOnce(ScheduledChore chore) {
        if (chore == null) {
            return false;
        }
        try {
            ScheduledFuture<?> future = scheduledChores.get(chore);
            if (future != null && !future.isDone()) {
                future.cancel(false);
            }
            chore.setChoreService(this);
            future = scheduler.schedule(() -> {
                //关于为什么run 而不是start 是为了保证同步，在下面移除chore
                chore.run();
                scheduledChores.remove(chore);
            }, chore.getInitialDelay(), chore.getUnit());
            scheduledChores.put(chore, future);
            return true;
        } catch (Exception e) {
            ErrorLog.error("Could not successfully schedule chore ", e);
        }
        return false;
    }

}
