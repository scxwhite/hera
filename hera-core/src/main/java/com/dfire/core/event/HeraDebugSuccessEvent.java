package com.dfire.core.event;

import com.dfire.common.entity.HeraDebugHistory;
import com.dfire.core.event.base.ApplicationEvent;
import com.dfire.core.event.base.Events;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:06 2018/4/19
 * @desc
 */
public class HeraDebugSuccessEvent extends ApplicationEvent {

    private HeraDebugHistory history;
    private String fileId;

    public HeraDebugSuccessEvent(String fileId,HeraDebugHistory history) {
        super(Events.JobSucceed);
        this.fileId=fileId;
        this.history=history;
    }

}
