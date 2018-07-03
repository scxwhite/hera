package com.dfire.core.event;

import com.dfire.core.event.base.ApplicationEvent;
import com.dfire.core.event.base.EventType;
import lombok.Data;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:20 2018/4/19
 * @desc
 */
@Data
public class HeraJobLostEvent extends ApplicationEvent {

    private final String jobId;
    public HeraJobLostEvent(EventType type, String jobId){
        super(type);
        this.jobId=jobId;
    }
}
