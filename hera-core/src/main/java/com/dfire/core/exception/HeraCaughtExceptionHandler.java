package com.dfire.core.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午3:23 2018/6/12
 * @desc
 */
@Slf4j
public class HeraCaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.error("caught thread exception " + e);
        System.out.println("捕获到异常");
        throw new RuntimeException(e);
    }
}
