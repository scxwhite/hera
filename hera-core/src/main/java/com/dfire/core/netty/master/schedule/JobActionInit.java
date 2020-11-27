package com.dfire.core.netty.master.schedule;

import com.dfire.core.netty.ScheduledChore;
import com.dfire.core.netty.master.Master;
import com.dfire.core.netty.master.constant.MasterConstant;
import com.dfire.logs.ErrorLog;
import org.joda.time.DateTime;

import java.util.concurrent.TimeUnit;

/**
 * desc: 任务版本的生成初始化
 *
 * @author scx
 * @create 2020/01/07
 */
public class JobActionInit extends ScheduledChore {

    private Master master;

    private JobActionInit(Master master, long initialDelay, long period, TimeUnit unit) {
        super("JobActionInit", initialDelay, period, unit);
        this.master = master;
        Thread thread = new Thread(() -> {
            try {
                TimeUnit.MINUTES.sleep(1);
            } catch (InterruptedException e) {
                ErrorLog.error(Thread.currentThread().getName() + "is interrupted", e);
            }
            //必须生成版本，否则在凌晨重启时可能会有问题
            master.generateBatchAction(true);
            master.clearInvalidAction();
        });
        thread.setName("first-start-action-init");
        thread.setDaemon(true);
        thread.start();
    }

    public JobActionInit(Master master) {
        this(master, 60 - new DateTime().getMinuteOfHour(), 60, TimeUnit.MINUTES);
    }


    @Override
    protected void chore() {
        this.master.generateBatchAction(false);
        if (DateTime.now().getHourOfDay() == MasterConstant.MORNING_TIME) {
            this.master.clearInvalidAction();

        }
    }

}
