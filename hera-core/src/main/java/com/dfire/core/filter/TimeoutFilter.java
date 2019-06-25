package com.dfire.core.filter;

import com.dfire.common.config.ExecuteFilter;
import com.dfire.common.config.Filter;
import com.dfire.common.config.ServiceLoader;
import com.dfire.common.service.JobFailAlarm;
import com.dfire.common.util.NamedThreadFactory;
import com.dfire.common.vo.JobElement;
import com.dfire.config.HeraGlobalEnv;
import com.dfire.logs.ErrorLog;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * desc:
 *
 * @author scx
 * @create 2019/05/08
 */
@Filter("timeoutFilter")
public class TimeoutFilter implements ExecuteFilter {


    private volatile Timer timeoutCheck;

    private ConcurrentHashMap<JobElement, Timeout> cache;

    @Override
    public void onExecute(JobElement jobElement) {
        if (jobElement.getCostMinute() == null || jobElement.getCostMinute() <= 0) {
            return;
        }
        if (timeoutCheck == null) {
            synchronized (this) {
                if (timeoutCheck == null) {
                    timeoutCheck = new HashedWheelTimer(
                            new NamedThreadFactory("timeout-check-timer", true),
                            1,
                            TimeUnit.SECONDS);
                    cache = new ConcurrentHashMap<>(HeraGlobalEnv.getMaxParallelNum());
                }
            }
        }
        cache.putIfAbsent(jobElement, timeoutCheck.newTimeout(timeout -> {
            List<JobFailAlarm> alarms = ServiceLoader.getAlarms();
            for (JobFailAlarm alarm : alarms) {
                try {
                    alarm.alarm(jobElement);
                } catch (Exception e) {
                    ErrorLog.error("告警失败:", e);
                }
            }
        }, jobElement.getCostMinute(), TimeUnit.MINUTES));
    }

    @Override
    public void onResponse(JobElement element) {
        if (cache != null) {
            Timeout remove = cache.remove(element);
            if (remove != null) {
                remove.cancel();
            }
        }
    }


}