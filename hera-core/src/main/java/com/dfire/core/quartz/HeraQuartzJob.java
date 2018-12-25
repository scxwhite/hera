package com.dfire.core.quartz;

import com.dfire.core.event.Dispatcher;
import com.dfire.core.event.HeraScheduleTriggerEvent;
import com.dfire.logs.ScheduleLog;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

/**
 * @author xiaosuda
 * @date 2018/6/26
 */
public class HeraQuartzJob implements Job {

    @Override
    public void execute(JobExecutionContext context) {
        String jobId = context.getJobDetail().getJobDataMap().getString("actionId");
        Dispatcher dispatcher = (Dispatcher) context.getJobDetail().getJobDataMap().get("dispatcher");
        HeraScheduleTriggerEvent scheduledEvent = HeraScheduleTriggerEvent.builder().jobId(jobId).build();
        dispatcher.forwardEvent(scheduledEvent);
        ScheduleLog.warn("execute schedule job {}", jobId);
    }
}
