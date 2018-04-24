package com.dfire.core.event;

import com.dfire.core.event.base.*;
import com.dfire.core.event.handler.AbstractHandler;
import com.dfire.core.event.handler.JobHandler;
import com.dfire.core.event.listenter.AbstractListener;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 11:00 2018/1/4
 * @desc hera中的任务事件总线，注册在spring中
 */
@Slf4j
@Component
public class Dispatcher extends AbstractObservable {

    public static final EventType beforeDispatch = new EventType();

    public static final EventType afterDispatch = new EventType();

    @Getter
    List<AbstractHandler> jobHandlers;

    public Dispatcher() {
        jobHandlers = new ArrayList<AbstractHandler>();
    }

    public void addJobHandler(JobHandler jobHandler) {
        if(!jobHandlers.contains(jobHandler)) {
            jobHandlers.add(jobHandler);
        }
    }

    public void removeJobhandler(JobHandler jobHandler) {
        if(jobHandlers.contains(jobHandler)) {
            jobHandlers.remove(jobHandler);
        }
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



    public void dispatch(ApplicationEvent applicationEvent) {
        MvcEvent event = new MvcEvent(this, applicationEvent);
        event.setApplicationEvent(applicationEvent);
        if(fireEvent(beforeDispatch, event)) {
            List<AbstractHandler> jobHandlersCopy = new ArrayList<>(jobHandlers);
            for(AbstractHandler jobHandler : jobHandlersCopy) {
                if(jobHandler.canHandle(applicationEvent)) {
                    if(! jobHandler.isInitialized()) {
                        jobHandler.setInitialized(true);
                    }
                    jobHandler.handleEvent(applicationEvent);
                }
            }
            fireEvent(afterDispatch, applicationEvent);
        }
    }


}
