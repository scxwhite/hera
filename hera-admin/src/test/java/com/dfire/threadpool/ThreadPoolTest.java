package com.dfire.threadpool;

import java.util.concurrent.*;

/**
 * Created by xiaosuda on 2018/11/7.
 */
public class ThreadPoolTest {
    private static CompletionService<Response> completionService = new ExecutorCompletionService<>(Executors.newCachedThreadPool());



    public static void main(String[] args) {

        for (int i = 0 ; i < 10; i++) {
            int time = i;
            completionService.submit(() -> {
                Response response = new Response();
                response.setX(time);
                TimeUnit.SECONDS.sleep(10 - time);
                return response;
            });
        }

        new Thread(() -> {

            while(true) {
                try {
                    System.out.println("1。。。。");
                    Future<Response> take = completionService.take();
                    System.out.println("2。。。。");
                    Response response = take.get();
                    System.out.println(response.getX());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}

class Response {

    private Integer x;

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getX() {
        return x;
    }
}
