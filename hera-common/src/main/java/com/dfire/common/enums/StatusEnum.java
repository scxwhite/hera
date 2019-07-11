package com.dfire.common.enums;


/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 22:41 2018/1/12
 * @desc 任务状态
 */
public enum StatusEnum {

    /**
     * 等待状态
     */
    WAIT("wait"),
    /**
     * 运行状态
     */
    RUNNING("running"),
    /**
     * 成功状态
     */
    SUCCESS("success"),
    /**
     * 失败状态
     */
    FAILED("failed");

    private String status;

    StatusEnum(String status) {
        this.status = status;
    }

    public static StatusEnum parse(String v) {
        for (StatusEnum s : StatusEnum.values()) {
            if (s.status.equalsIgnoreCase(v)) {
                return s;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return status;
    }
}
