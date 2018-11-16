package com.dfire.core.schedule;

import com.dfire.core.netty.master.MasterContext;
import com.dfire.logs.HeraLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 14:04 2018/1/12
 * @desc
 */
@Component
public class HeraSchedule {

    private AtomicBoolean running = new AtomicBoolean(false);

    @Autowired
    private MasterContext masterContext;


    public void startup() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        HeraLog.info("begin to start master context");
        masterContext.init();
    }

    public void shutdown() {
        if (running.compareAndSet(true, false)) {
            masterContext.destroy();
        }
    }

}
