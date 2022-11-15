package com.dfire.logs;

import lombok.extern.slf4j.Slf4j;

/**
 * @author xiaosuda
 * @date 2018/11/26
 */
@Slf4j
public class ErrorLog {


    public static void error(String msg) {
        log.error(msg);
    }

    public static void error(String format, Object... arguments) {
        log.error(format, arguments);
    }

    public static void error(String msg, Throwable e) {
        log.error(msg, e);
    }

    public static void warn(String format, Object... arguments) {
        log.warn(format, arguments);
    }

    public static void error(Throwable e) {
        log.error(e.getMessage(), e);
    }


}
