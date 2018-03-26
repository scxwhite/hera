package com.dfire.common.constant;


/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 22:41 2018/1/12
 * @desc 任务状态
 */
public enum Status {

    WAIT("wait"), RUNNING("running"), SUCCESS("success"), FAILED("failed");

    private String status;

    private Status(String status) {
        this.status = status;
    }

    public static Status parse(String v) {
        for(Status s:Status.values()){
            if(s.status.equalsIgnoreCase(v)){
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
