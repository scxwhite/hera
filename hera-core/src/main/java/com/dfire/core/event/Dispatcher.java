package com.dfire.core.event;

import com.dfire.core.event.base.AbstractObservable;
import com.dfire.core.event.base.MvcEvent;
import com.dfire.core.event.handler.AbstractHandler;
import com.dfire.core.event.handler.JobHandler;
import com.dfire.core.event.listenter.AbstractListener;
import com.dfire.event.ApplicationEvent;
import com.dfire.event.EventType;
import com.dfire.logs.ErrorLog;
import com.google.common.collect.Lists;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 11:00 2018/1/4
 * @desc hera中的任务事件observer, 接受事件，全局广播dispatch
 */
public class Dispatcher extends AbstractObservable {

    public static final EventType beforeDispatch = new EventType();

    public static final EventType afterDispatch = new EventType();

    @Getter
    Map<Long, AbstractHandler> jobHandlers;

    public Dispatcher() {
        jobHandlers = new ConcurrentHashMap<>();
    }

    public JobHandler addJobHandler(JobHandler jobHandler) {
        long jobId = jobHandler.getActionId();
        if (jobHandlers.containsKey(jobId)) {
            return (JobHandler) jobHandlers.get(jobId);
        } else {
            jobHandlers.put(jobId, jobHandler);
            return jobHandler;
        }
    }

    public void removeJobHandler(JobHandler jobHandler) {
        jobHandlers.remove(jobHandler.getActionId());
    }

    public void addDispatcherListener(AbstractListener listener) {
        addListener(beforeDispatch, listener);
        addListener(afterDispatch, listener);
    }


    public void forwardEvent(ApplicationEvent event) {
        dispatch(event);
    }

    public void forwardEvent(EventType eventType) {
        dispatch(eventType);
    }


    public void dispatch(EventType type) {
        dispatch(new ApplicationEvent(type));
    }

    public void dispatch(EventType type, Object data) {
        dispatch(new ApplicationEvent(type, data));
    }

    /**
     * 事件广播，每次任务状态变化，触发响应事件，全局广播，自动调度successEvent,触发依赖调度一些依赖更新
     *
     * @param applicationEvent
     */
    public void dispatch(ApplicationEvent applicationEvent) {
        try {
            MvcEvent mvcEvent = new MvcEvent(this, applicationEvent);
            mvcEvent.setApplicationEvent(applicationEvent);
            if (fireEvent(beforeDispatch, mvcEvent)) {
                List<AbstractHandler> jobHandlersCopy = Lists.newArrayList(jobHandlers.values());
                for (AbstractHandler jobHandler : jobHandlersCopy) {
                    try {
                        if (jobHandler.canHandle(applicationEvent)) {
                            if (!jobHandler.isInitialized()) {
                                jobHandler.setInitialized(true);
                            }
                            jobHandler.handleEvent(applicationEvent);
                        }
                    } catch (Exception e) {
                        ErrorLog.error(((JobHandler) jobHandler).getActionId() + "广播异常", e);
                    }
                }
                fireEvent(afterDispatch, mvcEvent);
            }
        } catch (Exception e) {
            ErrorLog.error("global dispatch job event error", e);
        }

    }

}
