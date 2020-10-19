package com.dfire.core.netty.enums;

/**
 * desc:
 *
 * @author scx
 * @create 2020/04/03
 */
public enum MessageEnum {


    /**
     * 更新重跑判断
     */
    UPDATE_RERUN_JUDGE(1),

    /**
     * 更新重跑最大并行度
     */
    UPDATE_RERUN_NUMS(2),

    /**
     * 更新任务的优先级
     */
    UPDATE_TASK_PRIORITY(3),
    /**
     * 更新机器组信息
     */
    UPDATE_WORK_INFO(4);

    Integer type;


    MessageEnum(Integer type) {
        this.type = type;
    }

    public static MessageEnum parser(Integer type) {
        for (MessageEnum msg : MessageEnum.values()) {
            if (msg.type.equals(type)) {
                return msg;
            }
        }
        return null;
    }

    public Integer getType() {
        return type;
    }

}
