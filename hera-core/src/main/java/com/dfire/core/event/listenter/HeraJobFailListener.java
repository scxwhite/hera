package com.dfire.core.event.listenter;

import com.dfire.common.util.NamedThreadFactory;
import com.dfire.core.event.HeraJobFailedEvent;
import com.dfire.core.event.base.MvcEvent;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.monitor.service.JobFailAlarm;

import java.util.ServiceLoader;
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
    private ServiceLoader<JobFailAlarm> alarms;
    //告警接口，待开发

    public HeraJobFailListener(MasterContext context) {
        alarms = ServiceLoader.load(JobFailAlarm.class);
        executor = new ThreadPoolExecutor(
                1, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(Integer.MAX_VALUE), new NamedThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
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
