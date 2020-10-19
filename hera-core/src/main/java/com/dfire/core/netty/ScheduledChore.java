package com.dfire.core.netty;

import com.dfire.logs.ErrorLog;

import java.util.concurrent.TimeUnit;

/**
 * desc:
 *
 * @author scx
 * @create 2020/01/06
 */
public abstract class ScheduledChore implements Runnable {

    private final Integer MAX_EXCEPTION_TIMES = 99;
    protected ChoreService choreService;

    private long initialDelay;
    private long period;
    private TimeUnit unit;
    private Integer exceptionTimes = 0;
    private String name;

    public ScheduledChore(String name, long initialDelay, long period, TimeUnit unit) {
        this.name = name;
        this.initialDelay = initialDelay;
        this.period = period;
        this.unit = unit;
    }

    protected void setInitialDelay(long initialDelay) {
        this.initialDelay = initialDelay;
    }


    protected String getName() {
        return name;
    }

    public long getInitialDelay() {
        return initialDelay;
    }

    public long getPeriod() {
        return period;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public void setChoreService(ChoreService service) {
        if (choreService != null && choreService != service) {
            choreService.cancelChore(this, false);
        }
        choreService = service;
    }

    @Override
    public void run() {
        try {
            chore();
        } catch (Exception e) {
            ErrorLog.error(name + " schedule chore exception:" + e);
            if (++exceptionTimes > MAX_EXCEPTION_TIMES) {
                String errorMsg = "cause exception for " + exceptionTimes + " times, stop it :" + name;
                ErrorLog.error(errorMsg);
                throw new RuntimeException(errorMsg);
            } else {
                exceptionTimes = 0;
            }
        }
    }

    protected abstract void chore();


}
