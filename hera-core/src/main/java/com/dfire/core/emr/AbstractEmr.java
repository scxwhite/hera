package com.dfire.core.emr;

import com.dfire.common.exception.HeraException;
import com.dfire.common.util.ActionUtil;
import com.dfire.common.util.NamedThreadFactory;
import com.dfire.config.HeraGlobalEnv;
import com.dfire.core.lock.DistributeLock;
import com.dfire.logs.ErrorLog;
import com.dfire.logs.MonitorLog;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;
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
    // protected volatile List<String> cacheIp = null;


    protected String prefixKey = "ssh -o StrictHostKeyChecking=no -i ";
    /**
     * 任务数
     */
    //  private volatile AtomicInteger taskRunning;
    /**
     * 任务计数器
     */
    //   private AtomicLong taskNum;
    /**
     * 缓存的集群Id
     */
    //  private volatile String cacheClusterId;
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
    //   private volatile boolean clusterTerminate = true;
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

    private ConcurrentHashMap<String, String> ownerCluster = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, AtomicLong> ownerTaskRunning = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, AtomicLong> ownerTaskTotal = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, List<String>> ownerIps = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, Object> ownerLock = new ConcurrentHashMap<>();


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

    protected String getClusterName(String owner) {
        return buildClusterName(owner) + ActionUtil.getCurrDate();
    }

    protected String buildClusterName(String owner) {
        return clusterPrefix + owner + "-";
    }

    /**
     * 创建集群的方法
     */
    protected void createCluster(String owner) {
        init();
        if (StringUtils.isBlank(ownerCluster.get(owner))) {
            synchronized (ownerLock.get(owner)) {
                if (notAlive(owner)) {
                    String cacheClusterId = null;
                    //创建集群有可能发生异常
                    while (StringUtils.isBlank(cacheClusterId)) {
                        cacheClusterId = sendCreateRequest(owner);
                        sleep();
                    }
                    boolean exception = false;
                    try {
                        waitClusterCompletion(cacheClusterId);
                        ownerCluster.put(owner, cacheClusterId);
                    } catch (HeraException e) {
                        ErrorLog.error("等待集群创建完成失败:", e);
                        sleep();
                        createCluster(owner);
                        exception = true;
                    }
                    //初始化一次就够了
                    if (!exception) {
                        before(owner);
                        MonitorLog.info("[" + owner + "]集群创建完成,可以执行任务了.集群ID为：" + ownerCluster.get(owner));
                    }
                }
            }
        } else {
            if (notAlive(owner)) {
                synchronized (ownerLock.get(owner)) {
                    if (notAlive(owner)) {
                        destroyCluster(owner);
                        createCluster(owner);
                    }
                }
            }
        }
    }

    private void before(String owner) {
        ownerIps.remove(owner);
        if (DistributeLock.isMaster()) {
            //重置当前集群的所有任务，running不可重置，因为可能集群被手动关闭，任务还在重试中
            ownerTaskTotal.putIfAbsent(owner, new AtomicLong(0));
            submitClusterWatch();
        }
    }


    @Override
    public void addJob(String owner) {
        try {
            ownerLock.putIfAbsent(owner, new Object());
            createCluster(owner);
        } catch (Exception e) {
            ErrorLog.error("创建集群失败", e);
        } finally {
            if (ownerTaskRunning.get(owner) != null) {
                ownerTaskRunning.get(owner).incrementAndGet();
            } else {
                ownerTaskRunning.putIfAbsent(owner, new AtomicLong(1));
            }
            if (ownerTaskTotal.get(owner) != null) {
                ownerTaskTotal.get(owner).incrementAndGet();
            } else {
                ownerTaskTotal.putIfAbsent(owner, new AtomicLong(1));
            }
        }
    }

    @Override
    public void removeJob(String owner) {
        if (ownerTaskRunning.get(owner) != null) {
            ownerTaskRunning.get(owner).decrementAndGet();
        }
    }


    /**
     * 判断是否有创建好的集群
     *
     * @return 结果
     */
    private boolean notAlive(String owner) {
        //如果当前缓存的集群已经关闭，查询是否有已经启动的
        String cacheClusterId;
        if (!checkAlive(cacheClusterId = ownerCluster.get(owner))) {
            String newClusterId = getAliveId(owner);
            //发生了集群id切换，重新检测。一个线程检测就够了
            if (StringUtils.isNotBlank(newClusterId)) {
                synchronized (ownerLock.get(owner)) {
                    try {
                        waitClusterCompletion(newClusterId);
                        ownerCluster.put(owner, newClusterId);
                        before(owner);
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
    public String getIp(String owner) {
        ownerLock.putIfAbsent(owner, new Object());
        this.init();
        if (notAlive(owner)) {
            throw new IllegalStateException("[" + owner + "]:无存活的集群");
        }
        List<String> ipList = ownerIps.get(owner);
        if (ipList == null || ipList.size() == 0) {
            synchronized (ownerLock.get(owner)) {
                if (ipList == null || ipList.size() == 0) {
                    ipList = getMasterIp(ownerCluster.get(owner));
                    if (ipList == null || ipList.size() == 0) {
                        throw new NullPointerException("cacheIp can not be null");
                    }
                    ownerIps.put(owner, ipList);
                }
            }
        }
        return ipList.get(ThreadLocalRandom.current().nextInt(ipList.size()));
    }


    /**
     * 循环检测 ，等待集群创建完成
     */
    private void waitClusterCompletion(String cacheClusterId) throws HeraException {
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
            HashMap<String, Long> cacheTotalNum = new HashMap<>(ownerCluster.size());
            clusterWatchFuture = pool.scheduleWithFixedDelay(() -> {
                ArrayList<String> owners = new ArrayList<>(ownerCluster.keySet());
                if (owners.size() == 0) {
                    MonitorLog.info("there has no emr cluster owner,shutdown cluster-destroy-watch pool");
                    clusterWatchFuture.cancel(true);
                    clusterWatchFuture = null;
                    cacheTotalNum.clear();
                } else {
                    for (String owner : owners) {
                        long taskRunning = ownerTaskRunning.getOrDefault(owner, new AtomicLong(0)).get();
                        //如果正在运行的任务数为0 ，判断所有运行任务是否有变化
                        if (taskRunning == 0) {
                            long lastTotal = cacheTotalNum.getOrDefault(owner, 0L);
                            long taskTotal = ownerTaskTotal.getOrDefault(owner, new AtomicLong(0)).get();
                            MonitorLog.info("isMaster:{},正在emr集群[{}]运行的任务个数:{},十分钟前运行的总任务个数:{},运行的总任务个数:{}", DistributeLock.isMaster(), owner, taskRunning, lastTotal, taskTotal);
                            //如果任务执行没有变化，说明可以关闭了
                            if (lastTotal == taskTotal) {
                                MonitorLog.info("[" + owner + "]集群:" + ownerCluster.get(owner) + "关闭成功,执行任务数为:" + taskTotal);
                                destroyCluster(owner);
                                cacheTotalNum.remove(owner);
                            } else {
                                cacheTotalNum.put(owner, taskTotal);
                            }
                        }
                    }
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
    protected abstract String sendCreateRequest(String owner);

    /**
     * 判断以clusterPrefix开头的机器是否有存活
     *
     * @return StringUtils.isBlank() 表示无存活
     */


    protected abstract String getAliveId(String owner);

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
    protected void destroyCluster(String owner) {
        clusterWatchFuture = null;
        ownerTaskRunning.remove(owner);
        ownerTaskTotal.remove(owner);
        if (ownerCluster.size() == 0) {
            shutdown();
        }
        terminateCluster(ownerCluster.remove(owner));
    }


    @Override
    public String getLogin(String user, String owner) {
        return prefixKey + HeraGlobalEnv.getKeyPath() + " " + user + "@" + this.getIp(owner);
    }

    @Override
    public String getFixLogin(String host) {
        if ("UE".equals(HeraGlobalEnv.getArea()) || "WE".equals(HeraGlobalEnv.getArea())) {
            return prefixKey + " /home/docker/conf/fixed.pem -p 33033 hdfs@" + host;
        }
        return prefixKey + " /home/docker/conf/fixed.pem hadoop@" + host;
    }


}
