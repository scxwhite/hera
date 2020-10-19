package com.dfire.core.netty.enums;

/**
 * desc:
 *
 * @author scx
 * @create 2020/04/04
 */
public enum RerunCheck {

    /**
     * 非重跑任务限制，即：当前队列中有其它任务等待，则不允许重跑任务执行
     */

    OTHER_TASK_CHECK(1),
    /**
     * 重跑并行度限制，即：当前执行的任务数达到RuntimeConf.maxRerunNum时不允许执行
     */
    RERUN_NUM_CHECK(2),

    /**
     * 上面两者同时生效
     */
    ALL(0);

    Integer type;

    RerunCheck(Integer type) {
        this.type = type;
    }

    public static RerunCheck parser(Integer type) {
        for (RerunCheck check : RerunCheck.values()) {
            if (check.type.equals(type)) {
                return check;
            }
        }
        return null;
    }

}
