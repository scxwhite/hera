package com.dfire.core.event.listenter;

import com.dfire.common.util.NamedThreadFactory;
import com.dfire.core.event.HeraJobFailedEvent;
import com.dfire.core.event.base.MvcEvent;
import com.dfire.monitor.config.ServiceLoader;
import com.dfire.monitor.service.JobFailAlarm;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 任务失败的预处理
 *
 * @author xiaosuda
 */
public class HeraJobFailListener extends AbstractListener {

    private Executor executor;
    //告警接口，待开发

    private List<JobFailAlarm> alarms;

    public HeraJobFailListener() {
        alarms = ServiceLoader.getAlarms();
        executor = new ThreadPoolExecutor(
                1, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(Integer.MAX_VALUE), new NamedThreadFactory("alarm-thread-pool", true), new ThreadPoolExecutor.AbortPolicy());
    }

    @Override
    public void beforeDispatch(MvcEvent mvcEvent) {
        if (mvcEvent.getApplicationEvent() instanceof HeraJobFailedEvent) {
            executor.execute(() -> {
                HeraJobFailedEvent failedEvent = (HeraJobFailedEvent) mvcEvent.getApplicationEvent();
                for (JobFailAlarm failAlarm : alarms) {
                    failAlarm.alarm(failedEvent.getActionId());
                }
            });


        }
    }
}
