package com.dfire.common.constant;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 14:31 2018/3/22
 * @desc
 */
public enum JobRunType {

    Shell("shell"), Hive("hive");
    private final String id;

    JobRunType(String s) {
        this.id = s;
    }

    @Override
    public String toString() {
        return id;
    }

    public static JobRunType parser(String v) {
        for (JobRunType type : JobRunType.values()) {
            if (type.toString().equals(v)) {
                return type;
            }
        }
        return null;
    }
}
