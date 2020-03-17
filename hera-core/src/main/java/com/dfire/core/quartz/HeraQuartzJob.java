package com.dfire.core.quartz;

import com.dfire.common.constants.Constants;
import com.dfire.core.event.Dispatcher;
import com.dfire.event.HeraScheduleTriggerEvent;
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
        Long actionId = context.getJobDetail().getJobDataMap().getLong(Constants.QUARTZ_ID);
        Dispatcher dispatcher = (Dispatcher) context.getJobDetail().getJobDataMap().get(Constants.QUARTZ_DISPATCHER);
        HeraScheduleTriggerEvent scheduledEvent = HeraScheduleTriggerEvent.builder().actionId(actionId).build();
        dispatcher.forwardEvent(scheduledEvent);
        ScheduleLog.info("execute schedule job {}", actionId);
    }
}
