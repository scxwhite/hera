package com.dfire.threadpool;

import com.dfire.common.vo.JobElement;
import org.junit.Test;

import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * desc:
 *
 * @author scx
 * @create 2019/04/15
 */
public class QueueTest {


    @Test

    public void test() throws InterruptedException {
        BlockingQueue<JobElement> scheduleQueue = new PriorityBlockingQueue<>(10000, Comparator.comparing(JobElement::getPriorityLevel).reversed());

        scheduleQueue.put(JobElement.builder().jobId(1L).priorityLevel(1).build());
        scheduleQueue.put(JobElement.builder().jobId(2L).priorityLevel(2).build());
        scheduleQueue.put(JobElement.builder().jobId(3L).priorityLevel(3).build());
        scheduleQueue.put(JobElement.builder().jobId(4L).priorityLevel(3).build());
        scheduleQueue.put(JobElement.builder().jobId(5L).priorityLevel(4).build());
        scheduleQueue.put(JobElement.builder().jobId(6L).priorityLevel(5).build());
        scheduleQueue.put(JobElement.builder().jobId(7L).priorityLevel(5).build());
        scheduleQueue.put(JobElement.builder().jobId(8L).priorityLevel(6).build());


        while(!scheduleQueue.isEmpty()) {
            Thread.sleep(500);

            JobElement take = scheduleQueue.take();

            System.out.println(take.getJobId());
        }
    }

}
