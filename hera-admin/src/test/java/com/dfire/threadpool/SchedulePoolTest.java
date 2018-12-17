package com.dfire.threadpool;

import org.joda.time.DateTime;
import org.joda.time.DurationFieldType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by xiaosuda on 2018/11/8.
 */
public class SchedulePoolTest {


    static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(10);

    public static void main(String[] args) {

        test6();

    }



    public static void test6() {
        CompletableFuture<String> one = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("first");
            return "first";
        });

        CompletableFuture<String> two = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("second");
            return "second";
        });

        CompletableFuture<Void> allOf = CompletableFuture.allOf(one, two);

        allOf.join();

        System.out.println("全部完成");

    }

    public static void test5() {

        int maxNum = 9000000;
        List<Integer> list = new ArrayList<>(maxNum);

        for (int i = 0; i <= maxNum; i++) {
            list.add(i);
        }

        long start = System.currentTimeMillis();
        list.parallelStream().forEach((x) -> {
            if (x == maxNum) {
                System.out.println("last");
            }
        });
        System.out.println(System.currentTimeMillis() - start);

    }


    public static void test4() {
        DateTime now = new DateTime();
        executor.scheduleWithFixedDelay(() -> {
            System.out.println(new DateTime());
        }, 60 - now.getSecondOfMinute(), 5, TimeUnit.MINUTES);
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
