package com.dfire.core.event;

import com.dfire.core.event.base.ApplicationEvent;
import com.dfire.core.event.base.EventType;
import lombok.Getter;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:23 2018/4/19
 * @desc
 */
public class HeraJobMaintenanceEvent extends ApplicationEvent {

    @Getter
    private final String id;

    public HeraJobMaintenanceEvent(EventType type, String id) {
        super(type);
        this.id = id;
    }
}
