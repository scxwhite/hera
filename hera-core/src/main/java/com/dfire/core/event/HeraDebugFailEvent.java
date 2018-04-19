package com.dfire.core.event;

import com.dfire.common.entity.HeraDebugHistory;
import com.dfire.core.event.base.ApplicationEvent;
import com.dfire.core.event.base.Events;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午3:34 2018/4/19
 * @desc
 */
public class HeraDebugFailEvent extends ApplicationEvent {

    private final HeraDebugHistory debugHistory;
    private final String fileId;
    private final Throwable throwable;

    public HeraDebugFailEvent(String fileId, HeraDebugHistory history, Throwable t) {
        super(Events.JobFailed);
        this.fileId = fileId;
        this.debugHistory = history;
        this.throwable = t;
    }
}
