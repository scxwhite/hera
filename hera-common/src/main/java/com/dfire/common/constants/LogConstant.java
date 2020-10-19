package com.dfire.common.constants;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午3:02 2018/6/19
 * @desc 某些固定状态下的任务日志内容
 */
public class LogConstant {

    public static final String SERVER_START_JOB_LOG = "启动服务器发现正在running状态，判断状态已经丢失，进行重试操作\n";

    public static final String WORK_DISCONNECT_LOG = "work断开连接，主动取消该任务\n";

    public static final String DEPENDENT_READY_LOG = "依赖任务全部到位，开始执行\n";

    public static final String SUPER_RECOVER_LOG = "超级恢复依赖触发\n";

    public static final String LOST_JOB_LOG = "漏跑任务,自动恢复执行\n";

    public static final String CHECK_QUEUE_LOG = "已经在调度队列中,无法再次运行,请稍后再试\n";

    public static final String CANCEL_JOB_LOG = "已经在队列中，无法再次运行\n";

    public static final String FAIL_JOB_RETRY = "失败任务重试，开始执行\n";

    public static final String RETRY_JOB = "重试任务\n";

}
