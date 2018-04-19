package com.dfire.core.event.base;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:57 2018/4/18
 * @desc
 */
public class EventType {

    private static int count = 0;
    final String id;

    public EventType() {
        id = String.valueOf(count++);
    }

}
