package com.dfire.event;

import lombok.Getter;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:23 2018/4/19
 * @desc
 */
public class HeraJobMaintenanceEvent extends ApplicationEvent {

    @Getter
    private final Long id;

    public HeraJobMaintenanceEvent(EventType type, Long id) {
        super(type);
        this.id = id;
    }
}
