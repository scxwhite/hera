package com.dfire.core.emr;

import com.dfire.common.exception.HeraException;
import com.dfire.common.util.ActionUtil;
import com.dfire.common.util.NamedThreadFactory;
import com.dfire.config.HeraGlobalEnv;
import com.dfire.core.lock.DistributeLock;
import com.dfire.logs.ErrorLog;
import com.dfire.logs.MonitorLog;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

/**
 * desc: emr集群 抽象类
 *
 * @author scx
 * @create 2019/04/01
 */
public abstract class AbstractEmr implements EmrJob, Emr {

    /**
     * emr集群的前缀
     */
    protected final String clusterPrefix = "hera-schedule-" + HeraGlobalEnv.getEnv() + "-";
    /**
     * 缓存的集群IP
     */
    protected volatile List<String> cacheIp = null;
    protected String prefixKey = "ssh -o StrictHostKeyChecking=no -i ";
    /**
     * 任务数
     */
    private volatile AtomicInteger taskRunning;
    /**
     * 任务计数器
     */
    private AtomicLong taskNum;
    /**
     * 缓存的集群Id
     */
    private volatile String cacheClusterId;
    /**
     * 关闭集群的调度池
     */
    private ScheduledExecutorService pool;
    /**
     * 上次的任务数
     */
    private long cacheTaskNum;
    /**
     * 集群是否已经关闭字段
     */
    private volatile boolean clusterTerminate = true;
    /**
     * check 集群是否需要关闭返回的future
     */
    private ScheduledFuture<?> clusterWatchFuture;
    /**
     * 自动扩展冷却时间 单位：秒
     */
    private int coolDown = 300;
    /**
     * 扩展百分比
     */
    private int scalePercent = 10;
    /**
     * 最少实例数
     */
    private int minCapacity = 1;
    /**
     * 最大实例数
     */
    private int maxCapacity = 15;


    public int getCoolDown() {
        return coolDown;
    }

    public int getScalePercent() {
        return scalePercent;
    }

    public int getMinCapacity() {
        return minCapacity;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    protected String getClusterName() {
        return clusterPrefix + ActionUtil.getCurrDate();
    }


    /**
     * 创建集群的方法
     */
    protected void createCluster() {
        init();
        if (clusterTerminate) {
            synchronized (this) {
                if (clusterTerminate) {
                    if (notAlive()) {
                        cacheClusterId = null;
                        //创建集群有可能发生异常
                        while (StringUtils.isBlank(cacheClusterId)) {
                            cacheClusterId = sendCreateRequest();
                            sleep();
                        }
                    }
                    boolean exception = false;
                    try {
                        waitClusterCompletion();
                    } catch (HeraException e) {
                        ErrorLog.error("等待集群创建完成失败:", e);
                        sleep();
                        createCluster();
                        exception = true;
                    }
                    //初始化一次就够了
                    if (!exception) {
                        before();
                        MonitorLog.info("集群创建完成,可以执行任务了.集群ID为：" + cacheClusterId);
                    }
                }
            }
        } else {
            if (notAlive()) {
                synchronized (this) {
                    if (notAlive()) {
                        destroyCluster();
                        createCluster();
                    }
                }
            }
        }
    }

    private void before() {
        if (DistributeLock.isMaster()) {
            if (taskRunning == null) {
                taskRunning = new AtomicInteger(0);
            }
            if (taskNum == null) {
                taskNum = new AtomicLong(0);
            }
            submitClusterWatch();
        }
        clusterTerminate = false;
    }


    @Override
    public boolean isAlive() {
        init();
        return !notAlive();
    }

    @Override
    public void addJob() {
        try {
            createCluster();
        } catch (Exception e) {
            ErrorLog.error("创建集群失败", e);
        } finally {
            if (taskRunning != null) {
                taskRunning.incrementAndGet();
            }
            if (taskNum != null) {
                taskNum.incrementAndGet();
            }
        }
    }

    @Override
    public void removeJob() {
        if (taskRunning != null) {
            taskRunning.decrementAndGet();
        }
    }


    /**
     * 判断是否有创建好的集群
     *
     * @return 结果
     */
    private boolean notAlive() {
        //如果当前缓存的集群已经关闭，查询是否有已经启动的
        if (!checkAlive(cacheClusterId)) {
            String newClusterId = getAliveId();
            //获得新的集群，缓存ip设置为null
            if (cacheClusterId != null && !cacheClusterId.equals(newClusterId)) {
                cacheIp = null;
            }
            //发生了集群id切换，重新检测。一个线程检测就够了
            if (StringUtils.isNotBlank(newClusterId)) {
                synchronized (this) {
                    try {
                        cacheClusterId = newClusterId;
                        waitClusterCompletion();
                        before();
                    } catch (HeraException e) {
                        ErrorLog.error("集群创建失败:", e);
                        return true;
                    }
                }
                return false;
            }
            MonitorLog.info("集群已经关闭:" + cacheClusterId);
            return true;
        }
        return false;
    }


    /**
     * 判断集群是否活着
     *
     * @param cacheClusterId cacheClusterId
     * @return boolean
     */
    protected abstract boolean checkAlive(String cacheClusterId);

    /**
     * 获得emr集群的登录ip/域名
     *
     * @return ip/域名
     */
    @Override
    public String getIp() {
        this.init();
        if (notAlive()) {
            throw new IllegalStateException("无存活的集群");
        }
        if (cacheIp == null) {
            synchronized (this) {
                if (cacheIp == null) {
                    cacheIp = getMasterIp(cacheClusterId);
                    if (cacheIp == null || cacheIp.size() == 0) {
                        throw new NullPointerException("cacheIp can not be null");
                    }
                }
            }
        }
        return cacheIp.get(ThreadLocalRandom.current().nextInt(cacheIp.size()));
    }


    /**
     * 循环检测 ，等待集群创建完成
     */
    private void waitClusterCompletion() throws HeraException {
        long start = System.currentTimeMillis();
        //最创建集群能容忍的最大等待时间 30 分钟
        long maxWaitTime = 30 * 60 * 1000L;
        // 必须同步 不能异步
        while (!isCompletion(cacheClusterId)) {
            sleep();
            if (System.currentTimeMillis() - start > maxWaitTime) {
                terminateCluster(cacheClusterId);
                throw new HeraException("创建集群时间超出" + maxWaitTime + "ms,强制关闭集群");
            }
        }
        MonitorLog.info("创建集群:" + cacheClusterId + "耗时:" + (System.currentTimeMillis() - start) + "ms");
    }

    private void sleep() {
        long sleepTime = 30 * 1000 * 1000000L;
        LockSupport.parkNanos(sleepTime);
    }


    /**
     * createCluster 方法已经同步过
     */
    private void submitClusterWatch() {
        if (clusterWatchFuture == null || clusterWatchFuture.isDone() || clusterWatchFuture.isCancelled()) {
            if (pool == null) {
                pool = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("cluster-destroy-watch", true));
            }
            cacheTaskNum = taskNum.get();
            clusterWatchFuture = pool.scheduleWithFixedDelay(() -> {
                MonitorLog.info("isMaster:{},正在emr集群运行的任务个数:{},十分钟前运行的总任务个数:{},运行的总任务个数:{}", DistributeLock.isMaster(), taskRunning.get(), cacheTaskNum, taskNum.get());
                if (taskRunning.get() == 0 && cacheTaskNum == taskNum.get()) {
                    terminateCluster(cacheClusterId);
                    MonitorLog.info("集群:" + cacheClusterId + "关闭成功,执行任务数为:" + taskNum.get());
                    clusterWatchFuture.cancel(true);
                    clusterWatchFuture = null;
                } else {
                    cacheTaskNum = taskNum.get();
                }
            }, 11, 11, TimeUnit.MINUTES);
        }
    }

    /**
     * 初始化client操作
     */
    protected abstract void init();

    /**
     * 关闭集群操作
     *
     * @param clusterId clusterId
     */
    protected abstract void terminateCluster(String clusterId);

    /**
     * 关闭client
     */
    protected abstract void shutdown();


    /**
     * 发送创建集群的请求
     *
     * @return 返回clusterId
     */
    protected abstract String sendCreateRequest();

    /**
     * 判断以clusterPrefix开头的机器是否有存活
     *
     * @return StringUtils.isBlank() 表示无存活
     */


    protected abstract String getAliveId();

    /**
     * 检测集群是否创建完成
     *
     * @param clusterId clusterId
     * @return
     */
    protected abstract boolean isCompletion(String clusterId) throws HeraException;

    /**
     * 获得master的ip
     *
     * @param clusterId clusterId
     * @return
     */
    protected abstract List<String> getMasterIp(String clusterId);

    /**
     * 销毁集群
     */
    protected synchronized void destroyCluster() {
        if (!clusterTerminate) {
            clusterTerminate = true;
            cacheIp = null;
            cacheClusterId = null;
            clusterWatchFuture = null;
            taskNum = null;
            taskRunning = null;
            pool.shutdown();
            pool = null;
            shutdown();
        }
    }

    @Override
    public String getLogin(String user, String ip) {
        return prefixKey + HeraGlobalEnv.getKeyPath() + " " + user + "@" + ip;
    }

    @Override
    public String getLogin(String user) {
        return prefixKey + HeraGlobalEnv.getKeyPath() + " " + user + "@" + this.getIp();
    }

    @Override
    public String getFixLogin(String host) {
        if ("UE".equals(HeraGlobalEnv.getArea())) {
            return prefixKey + " /home/docker/conf/fixed.pem -p 33033 hdfs@" + host;
        }
        return prefixKey + " /home/docker/conf/fixed.pem hadoop@" + host;
    }


}
