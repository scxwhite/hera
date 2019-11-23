package com.dfire.common.enums;

import java.util.Arrays;

/**
 * desc:
 *
 * @author scx
 * @create 2019/07/17
 */
public enum LogTypeEnum {


    /**
     * job类型的日志
     */
    JOB("job"),
    /**
     * group
     */
    GROUP("group"),
    /**
     * debug类型的日志
     */
    DEBUG("debug"),

    UPLOAD("upload"),
    /**
     * 用户类型的日志
     */
    USER("user");

    private String name;


    LogTypeEnum(String name) {
        this.name = name;
    }


    public static LogTypeEnum parseByName(String name) {
        return Arrays.stream(LogTypeEnum.values()).filter(typeEnum -> typeEnum.name.equals(name)).findAny().orElse(null);

    }

    public String getName() {
        return name;
    }
}