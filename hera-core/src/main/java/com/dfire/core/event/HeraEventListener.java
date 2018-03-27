package com.dfire.core.event;

import com.dfire.core.event.events.JobScheduledEvent;
import com.google.common.eventbus.Subscribe;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 11:05 2018/1/14
 * @desc hera事件监听
 */
public class HeraEventListener {

    @Subscribe
    public void scheduledEvent(JobScheduledEvent scheduledEvent) {

    }
}
