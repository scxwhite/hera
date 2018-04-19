package com.dfire.core.event.base;

import lombok.Builder;
import lombok.Data;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:56 2018/4/18
 * @desc
 */
@Data
@Builder

public abstract class AbstractEvent {

    private boolean cancelled;
    private Object source;
    private EventType type;

    public AbstractEvent(EventType type) {
        this.type = type;
    }

    public AbstractEvent(Object source) {
        this.source = source;
    }


}
