package com.dfire.common.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author xiaosuda
 * @date 2018/7/2
 */
public class NamedThreadFactory implements ThreadFactory {

    private final static AtomicInteger POOL_SEQ = new AtomicInteger(1);

    private final AtomicInteger mThreadNum = new AtomicInteger(1);


    private final boolean isDaemon;

    private final String mPrefix;

    private ThreadGroup mGroup;

    public NamedThreadFactory(){
        this("pool-" + POOL_SEQ.incrementAndGet(), false);
    }

    public NamedThreadFactory(String prefix) {
        this(prefix, false);
    }

    public NamedThreadFactory(String prefix, boolean isDaemon) {
        this.mPrefix = prefix + "-thread-";
        this.isDaemon = isDaemon;
        SecurityManager s = System.getSecurityManager();
        mGroup = (s == null) ? Thread.currentThread().getThreadGroup() : s.getThreadGroup();
    }

    @Override
    public Thread newThread(Runnable runnable) {
        String threadName = mPrefix + mThreadNum.incrementAndGet();
        Thread thread = new Thread(mGroup, runnable, threadName, 0);
        thread.setDaemon(isDaemon);
        return thread;
    }

    public ThreadGroup getThreadGroup() {
        return mGroup;
    }
}
