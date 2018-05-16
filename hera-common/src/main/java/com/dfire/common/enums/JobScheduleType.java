package com.dfire.common.enums;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午11:43 2018/4/23
 * @desc
 */
public enum JobScheduleType {
    Independent(0), Dependent(1);
    private Integer type;

    private JobScheduleType(Integer type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type.toString();
    }

    public static JobScheduleType parser(String value) {
        if ("0".equals(value)) {
            return Independent;
        }
        if ("1".equals(value)) {
            return Dependent;
        }
        return null;
    }

    public static JobScheduleType parser(Integer v) {
        for (JobScheduleType t : JobScheduleType.values()) {
            if (t.getType().equals(v)) {
                return t;
            }
        }
        return null;
    }

    public Integer getType() {
        return type;
    }
}

