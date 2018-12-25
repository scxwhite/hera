package com.dfire.logs;

import lombok.extern.slf4j.Slf4j;

/**
 * @author xiaosuda
 * @date 2018/11/5
 */
@Slf4j
public class SocketLog {
    public static void info(String msg) {
        log.info(msg);
    }


    public static void info(String format, Object... arguments) {
        log.info(format, arguments);
    }

    public static void error(String msg) {
        log.error(msg);
    }

    public static void error(String format, Object... arguments) {
        log.error(format, arguments);
    }


    public static void error(String msg, Exception e) {
        log.error(msg, e);
    }


    public static void warn(String msg) {
        log.warn(msg);
    }

    public static void warn(String format, Object... arguments) {
        log.warn(format, arguments);
    }

    public static void error(String format,Throwable throwable){
        log.error(format,throwable);
    }
}
