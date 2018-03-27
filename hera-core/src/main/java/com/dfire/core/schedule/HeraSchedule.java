package com.dfire.core.schedule;

import com.dfire.core.netty.master.MasterContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 14:04 2018/1/12
 * @desc
 */
@Slf4j
public class HeraSchedule {

    private AtomicBoolean running = new AtomicBoolean(false);

    private MasterContext masterContext;

    private ApplicationContext applicationContext;

    public HeraSchedule(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void startup(int port) {
        if(!running.compareAndSet(false, true)) {
            return;
        }
        log.info("begin to start master context");
        masterContext = (MasterContext) applicationContext.getBean("masterContext");
        masterContext.init(port);
    }

    public void shutdown() {
        if(running.compareAndSet(true, false)) {
            masterContext.destroy();
        }
    }

}
