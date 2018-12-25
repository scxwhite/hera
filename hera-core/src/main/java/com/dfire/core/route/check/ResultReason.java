package com.dfire.core.route.check;

/**
 * worker 检测的结果信息
 * @author <a href="mailto:huoguo@2dfire.com">火锅</a>
 * @time 2018/11/9
 */
public enum ResultReason {

    NULL_WORKER("节点不存在"),
    NULL_HEART("心跳信息为空"),
    MEM_LIMIT("内存超过限制"),
    LOAD_LIMIT("CPU LOAD 超过限制"),
    HOSTS_ERROR("hosts 不匹配"),
    TASK_LIMIT("运行任务数量超过限制");

    private  String msg;

    ResultReason(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return msg;
    }

    public String getMsg() {
        return msg;
    }
}
