package com.dfire.common.enums;

/**
 * @author xiaosuda
 * @date 2018/11/22
 */
public enum OperatorSystemEnum {
    /**
     * mac os系统
     */
    MAC,
    /**
     * linux 系统
     */
    LINUX,
    /**
     * windows 系统
     */
    WIN;


    public static boolean isLinux(OperatorSystemEnum systemEnum) {
        return systemEnum == LINUX;
    }

    public static boolean isMac(OperatorSystemEnum systemEnum) {
        return systemEnum == MAC;
    }


    public static boolean isWindows(OperatorSystemEnum systemEnum) {
        return systemEnum == WIN;
    }
}
