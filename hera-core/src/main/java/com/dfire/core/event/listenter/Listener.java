package com.dfire.core.event.listenter;

import com.dfire.event.AbstractEvent;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午5:52 2018/4/18
 * @desc
 */
public interface Listener<E extends AbstractEvent> {

    void handleEvent(E event);
}
