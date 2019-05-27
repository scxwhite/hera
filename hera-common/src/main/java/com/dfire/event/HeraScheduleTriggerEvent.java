package com.dfire.event;

import lombok.Builder;
import lombok.Data;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:32 2018/4/19
 * @desc
 */
@Builder
@Data
public class HeraScheduleTriggerEvent extends ApplicationEvent {

    private final String jobId;

    public HeraScheduleTriggerEvent(String jobId) {
        super(Events.ScheduleTrigger);
        this.jobId=jobId;
    }
}
