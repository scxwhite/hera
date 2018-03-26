package com.dfire.core.event;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 10:54 2018/1/14
 * @desc zeus中的任务事件总线，注册在spring中
 */
@Slf4j
@Component
public class ZeusEventBus {

    /**同步事件总线，默认选择方案，后期做性能测试，尝试异步事件总线，提高调度任务事件吞吐*/
    private static EventBus zeusSyncEventBus;

    /** 异步zeus任务事件总线 */
    private AsyncEventBus zeusAsyncEventBus = new AsyncEventBus(Executors.newCachedThreadPool());

    @PostConstruct
    public EventBus init() {
        zeusSyncEventBus = new EventBus();
        log.info("init zeus event bus success");
        return zeusSyncEventBus;
    }

    /**
     * 触发同步事件
     *
     * @param event
     */
    public static void post(Object event) {
        zeusSyncEventBus.post(event);
    }

    /**
     * 注册事件处理器
     *
     * @param handler
     */
    public static void register(Object handler) {
        zeusSyncEventBus.register(handler);
    }

    /**
     * 注销事件处理器
     *
     * @param handler
     */
    public static void unregister(Object handler) {
        zeusSyncEventBus.unregister(handler);
    }

}
