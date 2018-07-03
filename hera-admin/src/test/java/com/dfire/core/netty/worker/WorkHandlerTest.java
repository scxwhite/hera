package com.dfire.core.netty.worker;

import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by xiaosuda on 2018/6/27.
 */
public class WorkHandlerTest {


    @Test
    public void TestExecutorCompletionService() {
        ExecutorCompletionService<Integer> service = new ExecutorCompletionService<>(Executors.newCachedThreadPool());

        for (int i = 0; i < 10; i++) {
            int index = i;
            service.submit(() -> {
                if (index == 0) {
                    throw new RuntimeException();
                }
                return index;
            });
        }


        for (; ; ) {
            Future<Integer> future = service.poll();
            try {
                if (future.isDone()) {
                    System.out.println(future.get());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

}