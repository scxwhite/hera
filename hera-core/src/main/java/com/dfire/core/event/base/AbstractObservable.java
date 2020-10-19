package com.dfire.core.event.base;

import com.dfire.core.event.listenter.Listener;
import com.dfire.event.AbstractEvent;
import com.dfire.event.EventType;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午2:44 2018/4/23
 * @desc
 */
public class AbstractObservable implements Observable {

    private boolean fireEvents = true;

    private Map<String, List<Listener<AbstractEvent>>> listMap;

    private boolean activeEvent;


    @Override
    public void addListener(EventType eventType, Listener<? extends AbstractEvent> listener) {
        if (listener == null) {
            return;
        }
        if (listMap == null) {
            listMap = new HashMap<>(8);
        }
        List<Listener<AbstractEvent>> listeners = listMap.get(eventType.getId());
        if (listeners == null) {
            listeners = new ArrayList<>();
            listeners.add((Listener) listener);
            listMap.put(eventType.getId(), listeners);
        } else {
            if (!listeners.contains(listener)) {
                listeners.add((Listener) listener);
            }
        }

    }

    @Override
    public List<Listener<? extends AbstractEvent>> getListeners(EventType eventType) {
        if (listMap == null) {
            return new ArrayList<>();
        }
        List<Listener<AbstractEvent>> list = listMap.get(eventType.getId());
        if (list == null) {
            return new ArrayList<>();
        }
        return (List) list;
    }

    @Override
    public boolean hasListeners() {
        return listMap != null && listMap.size() > 0;
    }

    @Override
    public boolean hasListeners(EventType eventType) {
        if (listMap != null) {
            List<Listener<AbstractEvent>> listeners = listMap.get(eventType.getId());
            if (listeners != null && !listeners.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void removeAllListeners() {
        if (listMap != null) {
            listMap.clear();
        }

    }

    @Override
    public void removeAllListeners(EventType eventType, Listener<? extends AbstractEvent> listener) {
        if (listMap == null) {
            return;
        }
        String id = eventType.getId();
        List<Listener<AbstractEvent>> listeners = listMap.get(id);
        if (listeners != null) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                listMap.remove(id);
            }
        }
    }

    @Override
    public boolean fireEvent(EventType eventType, AbstractEvent abstractEvent) {
        if (fireEvents && listMap != null) {
            activeEvent = true;
            abstractEvent.setType(eventType);
            List<Listener<AbstractEvent>> listeners = listMap.get(eventType.getId());
            if (listeners != null) {
                List<Listener<AbstractEvent>> listenersCopy = Lists.newArrayList(listeners);
                for (Listener<AbstractEvent> listener : listenersCopy) {
                    callListener(listener, abstractEvent);
                }
            }
            activeEvent = false;
            return !abstractEvent.isCancelled();
        }
        return true;
    }

    public void callListener(Listener<AbstractEvent> listener, AbstractEvent event) {
        listener.handleEvent(event);
    }


}
