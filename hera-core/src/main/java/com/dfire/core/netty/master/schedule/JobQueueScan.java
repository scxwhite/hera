package com.dfire.core.netty.master.schedule;

import com.dfire.config.HeraGlobalEnv;
import com.dfire.core.netty.ScheduledChore;
import com.dfire.core.netty.master.Master;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.logs.ScanLog;
import com.dfire.logs.ScheduleLog;

import java.util.concurrent.TimeUnit;

/**
 * desc:任务队列扫描
 *
 * @author scx
 * @create 2020/01/09
 */
public class JobQueueScan extends ScheduledChore {

    /**
     * scan频率递增的步长
     */
    private final Integer DELAY_TIME = 100;
    /**
     * 最大scan频率
     */
    private final Integer MAX_DELAY_TIME = 10 * 1000;
    private Integer nextTime = HeraGlobalEnv.getScanRate();
    private Master master;
    private MasterContext masterContext;


    private JobQueueScan(Master master, long initialDelay, long period, TimeUnit unit) {
        super("JobQueueScan", initialDelay, period, unit);
        this.master = master;
        this.masterContext = master.getMasterContext();
    }

    public JobQueueScan(Master master) {
        this(master, 60, HeraGlobalEnv.getScanRate(), TimeUnit.MILLISECONDS);
    }

    @Override
    protected void chore() {
        try {
            if (master.getMasterContext().isStop()) {
                ScheduleLog.info("master is on stop status,stop run JobQueueScan");
                return;
            }
            // 每次add任务都需要检测isTaskLimit 是否已经满了 ，并且要优先遍历schedule队列
            boolean scheduleTake = !master.isTaskLimit() && master.scanQueue(masterContext.getScheduleQueue());
            boolean superTask = !master.isTaskLimit() && master.scanQueue(masterContext.getSuperRecovery());
            boolean manualTake = !master.isTaskLimit() && master.scanQueue(masterContext.getManualQueue());
            boolean debugTake = !master.isTaskLimit() && master.scanQueue(masterContext.getDebugQueue());
            boolean rerunTake = false;
            //rerunQueue优先级最低，只有当手动执行、超级恢复、手动恢复、开发中心无任务执行时才判断
            if (!scheduleTake && !superTask && !manualTake && !debugTake && !master.isTaskLimit()) {
                if (master.getRunningTaskNum() < HeraGlobalEnv.getMaxRerunParallelNum()) {
                    rerunTake = master.scanQueue(masterContext.getRerunQueue());
                }
            }
            if (scheduleTake || manualTake || debugTake || rerunTake) {
                nextTime = HeraGlobalEnv.getScanRate();
            } else {
                nextTime = (nextTime + DELAY_TIME) > MAX_DELAY_TIME ? MAX_DELAY_TIME : nextTime + DELAY_TIME;
            }
            this.setInitialDelay(nextTime);
        } catch (Exception e) {
            ScanLog.error("scan waiting queueTask exception", e);
        } finally {
            choreService.scheduledChoreOnce(this);
        }
    }
}
