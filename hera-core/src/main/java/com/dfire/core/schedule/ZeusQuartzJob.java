package com.dfire.core.schedule;

import com.dfire.core.event.Dispatcher;
import com.dfire.core.event.events.JobScheduledEvent;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 17:06 2018/3/21
 * @desc
 */
public class ZeusQuartzJob implements Job {
    @Override
    public void execute(JobExecutionContext context)  {

        String jobId = context.getJobDetail().getJobDataMap().getString("jobId");
        Dispatcher dispatcher = (Dispatcher) context.getJobDetail().getJobDataMap().get("dispatcher");
        JobScheduledEvent scheduledEvent = JobScheduledEvent.builder().jobId(jobId).build();

        System.out.println("");

    }
}
