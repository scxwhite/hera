package com.dfire.common.exception;

import com.dfire.logs.ErrorLog;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午3:23 2018/6/12
 * @desc
 */
public class HeraCaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        ErrorLog.error("Thread pool caught thread exception " + e);
        throw new RuntimeException(e);
    }
}
