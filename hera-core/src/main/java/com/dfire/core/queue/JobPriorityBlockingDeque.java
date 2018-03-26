package com.dfire.core.queue;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 19:53 2018/1/10
 * @desc 自定义实现一个任务优先级队列，依据任务的优先级入队列
 */
public class JobPriorityBlockingDeque {

    private final int capacity = 1 << 14;

    private final LinkedList<JobElement> list;

    private final ReentrantLock lock = new ReentrantLock();

    public Map<String, Long> jobs = new ConcurrentHashMap<String, Long>();

    private Comparator<JobElement> comparator;

    public JobPriorityBlockingDeque() {
        this.list = new LinkedList<JobElement>();
        this.comparator = new Comparator<JobElement>() {
            public int compare(JobElement left, JobElement right) {
                if (left.getJobId().equals(right.getJobId())) {
                    return 0;
                }
                int compare = left.getPriorityLevel().compareTo(right.getPriorityLevel());
                if (compare != 0) {
                    return compare;
                }
                compare = left.getTriggerTime().compareTo(right.getTriggerTime());
                if (compare != 0) {
                    return compare;
                }
                compare = left.getGmtModified().compareTo(right.getGmtModified());
                if (compare != 0) {
                    return compare;
                }

                return -1;
            }
        };
    }

    public JobElement pollFirst() {
        lock.lock();
        try {
            JobElement jobElement = list.pollFirst();
            if (jobElement == null) {
                return null;
            }
            jobs.remove(jobElement.getJobId());
            return jobElement;
        } finally {
            lock.unlock();
        }
    }

    public JobElement pollLast() {
        lock.lock();
        try {
            JobElement jobElement = list.pollLast();
            if (jobElement == null) {
                return null;
            }
            jobs.remove(jobElement.getJobId());
            return jobElement;
        } finally {
            lock.unlock();
        }
    }

    public boolean offer(JobElement jobElement) {
        if (list.size() > capacity) { //本来要执行扩容，暂时略
            return false;
        }
        if (jobElement == null)
            throw new NullPointerException();
        lock.lock();
        try {
            if (jobs.containsKey(jobElement.getJobId())) {
                Long gmtModified = jobs.get(jobElement.getJobId());
                if (gmtModified != null && !gmtModified.equals(jobElement.getGmtModified())) {
                    removeOld(jobElement);
                }
            }
            int pos = Collections.binarySearch(list, jobElement, comparator);
            if (pos < 0) {
                pos = -pos - 1;
            }
            list.add(pos, jobElement);
            jobs.put(jobElement.getJobId(), jobElement.getGmtModified());
            return true;
        } finally {
            lock.unlock();
        }


    }

    public void removeOld(JobElement jobElement) {
        Iterator<JobElement> iterator = iterator();
        int index = 0;
        while (iterator.hasNext()) {
            JobElement jobElementOld = iterator.next();
            if (jobElementOld.equals(jobElement)) {
                list.remove(index);
                jobs.remove(jobElement.getJobId());
            }
            index++;
        }
    }

    public JobElement poll() {
        return pollFirst();
    }

    public int size() {
        lock.lock();
        try {
            return list.size();
        } finally {
            lock.unlock();
        }
    }

    public Iterator<JobElement> iterator() {
        return list.iterator();
    }

    @Override
    public String toString() {
        lock.lock();
        try {
            Iterator<JobElement> iterator = iterator();
            if (!iterator.hasNext()) {
                return "[]";
            }
            StringBuilder stringBuilder = new StringBuilder();
            for (; ; ) {
                JobElement jobElement = iterator.next();
                stringBuilder.append(jobElement);
                if (!iterator.hasNext()) {
                    return stringBuilder.append("]").toString();
                }
                stringBuilder.append(",");
            }
        } finally {
            lock.unlock();
        }
    }

}
