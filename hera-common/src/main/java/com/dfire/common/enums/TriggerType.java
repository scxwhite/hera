package com.dfire.common.enums;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 23:21 2018/1/12
 * @desc 任务触发类型
 */
public enum TriggerType {

    SCHEDULE(1), MANUAL(2), MANUAL_RECOVER(3);
    private Integer id;

    private TriggerType(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id.toString();
    }

    public String toName() {
        if (id == 1) {
            return "自动调度";
        } else if (id == 2) {
            return "手动触发";
        } else if (id == 3) {
            return "手动恢复";
        }
        return "未知";
    }

    public static TriggerType parser(Integer v) {
        for (TriggerType type : TriggerType.values()) {
            if (type.getId().equals(v)) {
                return type;
            }
        }
        return null;
    }

    public Integer getId() {
        return id;
    }
}
