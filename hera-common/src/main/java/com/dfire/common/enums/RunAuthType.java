package com.dfire.common.enums;

/**
 * desc:
 *
 * @author scx
 * @create 2019/06/20
 */
public enum RunAuthType {

    /**
     * 任务类型
     */
    JOB("job"),
    /**
     * 组类型
     */
    GROUP("group");

    private String name;


    RunAuthType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}