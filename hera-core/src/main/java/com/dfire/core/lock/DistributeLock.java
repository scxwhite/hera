package com.dfire.core.lock;

import com.dfire.common.entity.HeraLock;
import com.dfire.common.service.HeraHostGroupService;
import com.dfire.common.service.HeraLockService;
import com.dfire.core.netty.worker.WorkClient;
import com.dfire.core.schedule.HeraSchedule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 20:47 2018/1/10
 * @desc 基于数据库实现的分布式锁方案，后面优化成基于redis实现分布式锁
 */
@Slf4j
@Component
public class DistributeLock {

    public static String host = "LOCALHOST";

    public static  int port = 7979;
    @Autowired
    private HeraHostGroupService hostGroupService;
    @Autowired
    private HeraLockService heraLockService;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private WorkClient workClient;

    public static boolean isMaster = false;

    private HeraSchedule heraSchedule;



    static {
        try {
            host = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @PostConstruct
    public void init() {
        heraSchedule = new HeraSchedule(applicationContext);
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        service.scheduleAtFixedRate(() -> getLock() , 5, 15, TimeUnit.SECONDS);
    }

    public void getLock() {
        HeraLock heraLock = heraLockService.getHeraLock("online");
        if (heraLock == null) {
            heraLock = HeraLock.builder()
                    .host(host)
                    .serverUpdate(new Date())
                    .build();
            heraLockService.save(heraLock);
        }
        isMaster = host.equals(heraLock.getHost().trim());
        if (isMaster) {
            heraLock.setServerUpdate(new Date());
            heraLockService.save(heraLock);
            log.info("hold lock and update time");
            heraSchedule.startup(port);
            workClient.shutdown();
        } else {
            log.info("not my lock");
            long currentTime = System.currentTimeMillis();
            long lockTime = heraLock.getServerUpdate().getTime();
            long interval = currentTime - lockTime;
            //host不匹配，切服务器更新时间间隔超过5分钟,判断发生master  切换
            if (interval > 1000 * 60 * 5L && isPreemptionHost()) {

                heraLock.setHost(host);
                heraLock.setServerUpdate(new Date());
                heraLock.setSubGroup("online");
                heraLockService.save(heraLock);
                log.info("master 发生切换");
            } else {
                if (workClient.getIsShutdown().get()) {
                    workClient = new WorkClient();
                }
                heraSchedule.shutdown();//非主节点，调度器不执行
                try {
                    //work连接master
                    workClient.connect(heraLock.getHost(), port);
                } catch (Exception e) {
                    log.info("client worker connect master server exception:{}", e);
                }
            }

        }
    }

    public boolean isPreemptionHost() {
        List<String> preemptionHostList = hostGroupService.getPreemptionGroup("1");
        if (preemptionHostList.contains(host)) {
            return true;
        } else {
            log.info(host + "is not in master group " + preemptionHostList.toString());
            return false;

        }
    }

}
