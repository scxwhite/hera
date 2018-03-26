package com.dfire;

import org.junit.Test;
import com.dfire.core.queue.JobElement;
import com.dfire.core.queue.JobPriorityBlockingDeque;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 10:29 2018/1/11
 * @desc 任务优先级队列测试
 */
public class JobQueueTest {

    @Test
    public void testQueue() throws InterruptedException {
        final JobPriorityBlockingDeque deque = new JobPriorityBlockingDeque();
        for (int i = 0; i < 100; i++) {
            JobElement jobElement = JobElement.builder()
                    .jobId(i + "")
                    .hostGroupId("1")
                    .priorityLevel(i)
                    .priorityLevel(new Random().nextInt(10 - 1) + 1)
                    .triggerTime(System.currentTimeMillis())
                    .gmtCreated(System.currentTimeMillis())
                    .gmtModified(System.currentTimeMillis())
                    .build();
            deque.offer(jobElement);
            Thread.sleep(10);
        }

        for (int i = 0, length = deque.size(); i < length; i++) {
        }


        ExecutorService service = Executors.newFixedThreadPool(3);
        service.submit(new Callable<JobElement>() {
            public JobElement call() throws Exception {
                return deque.pollLast();
            }
        });
        service.submit(new Callable<JobElement>() {
            public JobElement call() throws Exception {
                return deque.poll();
            }
        });
        System.out.println("end");

    }

}
