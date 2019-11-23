package com.dfire.core.event.listenter;

import com.dfire.common.util.NamedThreadFactory;
import com.dfire.core.event.Dispatcher;
import com.dfire.core.event.base.MvcEvent;
import com.dfire.event.EventType;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午5:57 2018/4/18
 * @desc
 */
public abstract class AbstractListener implements Listener<MvcEvent> {

    private Executor executor;


    @Override
    public void handleEvent(MvcEvent event) {
        EventType eventType = event.getType();
        if (eventType == Dispatcher.beforeDispatch) {
            beforeDispatch(event);
        } else if (eventType == Dispatcher.afterDispatch) {
            afterDispatch(event);
        }

    }

    public void beforeDispatch(MvcEvent mvcEvent) {

    }

    public void afterDispatch(MvcEvent mvcEvent) {

    }

    public Executor getSinglePool() {
        if (executor == null) {
            synchronized (this) {
                if (executor == null) {
                    executor = new ThreadPoolExecutor(
                            1, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(Integer.MAX_VALUE), new NamedThreadFactory("listener-thread-pool", true), new ThreadPoolExecutor.AbortPolicy());
                }
            }
        }
        return executor;
    }
}
