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

    @Autowired
    private HeraHostGroupService hostGroupService;
    @Autowired
    private HeraLockService heraLockService;
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private WorkClient workClient;

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
        ScheduledExecutorService service = Executors.newScheduledThreadPool(3);
        service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                getLock();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public void getLock() {
        log.info("start get lock");
        HeraLock heraLock = heraLockService.getHeraLock("online");
        if (heraLock == null) {
            heraLock = HeraLock.builder()
                    .host(host)
                    .serverUpdate(new Date())
                    .build();
            heraLockService.update(heraLock);
        }


        if (host.equals(heraLock.getHost().trim())) {
            heraLock.setServerUpdate(new Date());
            heraLockService.update(heraLock);
            log.info("hold lock and update time");
            heraSchedule.startup();
        } else {
            log.info("not my lock");
            long currentTime = System.currentTimeMillis();
            long lockTime = heraLock.getServerUpdate().getTime();
            long interval = currentTime - lockTime;
            if (interval > 1000 * 60 * 5L && isPreemptionHost()) {
                log.info("server lock time exceed 5 minutes and will happen master switch");
                heraLock.setHost(host);
                heraLock.setServerUpdate(new Date());
                heraLock.setSubGroup("online");
                heraLockService.update(heraLock);
                log.error("master 发生切换");
            } else {
                heraSchedule.shutdown();//非主节点，调度器不执行
                log.info("shutdown worker's  schedule service ");
            }
            try {

                log.info("work try to connect master ....");
                workClient.connect(heraLock.getHost());
                log.info("work connect master success...");
            } catch (Exception e) {
                log.info("client worker connect master server exception:{}", e);
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

    public static void main(String[] args) {
        System.out.println("ss ");
    }

}
