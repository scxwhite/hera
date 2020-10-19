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

    private final Long actionId;

    public HeraScheduleTriggerEvent(Long actionId) {
        super(Events.ScheduleTrigger);
        this.actionId = actionId;
    }
}
