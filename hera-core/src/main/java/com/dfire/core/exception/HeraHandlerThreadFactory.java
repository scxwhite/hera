package com.dfire.core.exception;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadFactory;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午5:39 2018/6/11
 * @desc 线程池异常处理器，用以捕获线程池中线程执行过程中的异常
 */
@Slf4j
public class HeraHandlerThreadFactory implements ThreadFactory {

    @Override
    public Thread newThread(Runnable r) {
        System.out.println("创建一个新的线程");
        Thread t = new Thread(r);
        t.setUncaughtExceptionHandler(new HeraCaughtExceptionHandler());
        log.info("eh121 = " + t.getUncaughtExceptionHandler());
        return t;
    }
}
