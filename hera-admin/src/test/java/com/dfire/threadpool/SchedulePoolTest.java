package com.dfire.threadpool;

import org.joda.time.DateTime;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by xiaosuda on 2018/11/8.
 */
public class SchedulePoolTest {


    static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(10);

    public static void main(String[] args) {

        test3();

    }


    /**
     * 根据上一次的成功时间来计时下一次
     */
    public static void test3() {
        executor.scheduleWithFixedDelay(() -> {
            System.out.println(DateTime.now());
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    /**
     * 只执行一次
     */
    public static void test2() {
        executor.schedule(() -> {
            System.out.println(DateTime.now());
        }, 2, TimeUnit.SECONDS);
    }

    /**
     * 根据程序的具体执行时间
     */
    public static void test1() {
        executor.scheduleAtFixedRate(() -> {
            System.out.println(DateTime.now());
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
}
