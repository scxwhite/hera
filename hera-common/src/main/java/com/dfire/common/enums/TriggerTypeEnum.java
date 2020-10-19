package com.dfire.common.enums;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 23:21 2018/1/12
 * @desc 任务触发类型
 */
public enum TriggerTypeEnum {

    NONE(0, "无"),
    /**
     * 定时任务
     */
    SCHEDULE(1, "自动调度"),
    /**
     * 手动执行任务
     */
    MANUAL(2, "手动触发"),
    /**
     * 手动恢复任务
     */
    MANUAL_RECOVER(3, "手动恢复"),
    /**
     * 开发中心任务
     */
    DEBUG(4, "开发执行"),
    /**
     * 自动重跑
     */

    AUTO_RERUN(5, "自动重跑"),
    /**
     * 超级恢复
     */
    SUPER_RECOVER(6, "超级恢复");

    private Integer id;

    private String name;

    TriggerTypeEnum(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public static TriggerTypeEnum parser(Integer v) {
        for (TriggerTypeEnum type : TriggerTypeEnum.values()) {
            if (type.getId().equals(v)) {
                return type;
            }
        }
        return NONE;
    }

    @Override
    public String toString() {
        return id.toString();
    }

    public String toName() {
        return name;
    }

    public Integer getId() {
        return id;
    }
}
