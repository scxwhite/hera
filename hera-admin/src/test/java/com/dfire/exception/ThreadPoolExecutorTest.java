package com.dfire.exception;

import java.util.concurrent.*;

/**
 * Created by xiaosuda on 2018/7/23.
 */
public class ThreadPoolExecutorTest {

    public static void main(String[] args) {
        int core = 2;
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2 * core, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.AbortPolicy());

        Future<String> one = executor.submit(() -> {
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "第一个测试：慢请求";
        });

        Future<String> two = executor.submit(() -> {
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "第二个测试：慢请求";
        });

        Future<String> three = executor.submit(() -> {
            return "快请求";
        });

        try {
            System.out.println("--------尝试获得第一个-------------");
            String s = one.get(5, TimeUnit.SECONDS);
            System.out.println(s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
            one.cancel(true);
            System.out.println("activeCount:" + executor.getActiveCount() + " largestPoolSize:" + executor.getLargestPoolSize());

        }

        try {
            System.out.println("--------尝试获得第二个-------------");
            String s = two.get(5, TimeUnit.SECONDS);
            System.out.println(s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
            two.cancel(true);
            System.out.println("activeCount:" + executor.getActiveCount() + " coresNum:" + executor.getLargestPoolSize());
        }

        try {
            System.out.println("--------尝试获得第三个-------------");
            String s = three.get(5, TimeUnit.SECONDS);
            System.out.println(s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
            three.cancel(true);
            System.out.println("activeCount:" + executor.getActiveCount() + " coresNum:" + executor.getLargestPoolSize());
        }

        executor.shutdown();

    }
}
