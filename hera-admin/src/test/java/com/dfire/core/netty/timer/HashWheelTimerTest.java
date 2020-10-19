package com.dfire.core.netty.timer;

import com.dfire.common.util.NamedThreadFactory;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午7:01 2018/8/29
 * @desc
 */
public class HashWheelTimerTest {

    @Test
    public void test3() throws InterruptedException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        HashedWheelTimer timer = new HashedWheelTimer(new NamedThreadFactory("timer-task"), 1, TimeUnit.MILLISECONDS,8);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                System.out.println("hello world" + LocalDateTime.now().format(formatter));
                timer.newTimeout(this, 2, TimeUnit.SECONDS);
            }
        };

        TimerTask timerTask2 = new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                float error = 3 / 2;
                System.out.println(error);
                timer.newTimeout(this, 2, TimeUnit.SECONDS);
            }
        };
        timer.newTimeout(timerTask, 4, TimeUnit.SECONDS);
        timer.newTimeout(timerTask2, 4, TimeUnit.SECONDS);
        System.out.println("------");

        Thread.currentThread().join();
    }


    @Test
    public void schedulePool() throws InterruptedException {
        ScheduledExecutorService schedule = Executors.newScheduledThreadPool(2);
        Runnable runnable1 = () -> System.out.println("hello world");
        Runnable runnable2 = () -> {
            float error = 3 / 0;
            System.out.println(error);
        };
        schedule.scheduleAtFixedRate(runnable1, 1, 2, TimeUnit.SECONDS);
        schedule.scheduleAtFixedRate(runnable2, 1, 3, TimeUnit.SECONDS);

        Thread.currentThread().join();

    }
}
