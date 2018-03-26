package com.dfire.core.lock;

import com.dfire.common.entity.ZeusLock;
import com.dfire.common.service.ZeusLockService;
import com.dfire.core.netty.worker.WorkClient;
import com.dfire.core.schedule.ZeusSchedule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import com.dfire.common.service.ZeusHostGroupService;

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
    private ZeusHostGroupService hostGroupService;
    @Autowired
    private ZeusLockService zeusLockService;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private WorkClient workClient;

    private ZeusSchedule zeusSchedule;

    private int port = 7979;

    static {
        try {
            host = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @PostConstruct
    public void init() {
        zeusSchedule = new ZeusSchedule(applicationContext);
        ScheduledExecutorService service = Executors.newScheduledThreadPool(2);
        service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                getLock();
            }
        }, 20, 60, TimeUnit.SECONDS);
    }

    public void getLock() {
        ZeusLock zeusLock = zeusLockService.getZeusLock("online");
        if (zeusLock == null) {
            zeusLock = ZeusLock.builder()
                    .host(host)
                    .serverUpdate(new Date())
                    .build();
            zeusLockService.save(zeusLock);
        }

        if (host.equals(zeusLock.getHost())) {
            zeusLock.setServerUpdate(new Date());
            zeusLockService.save(zeusLock);
            log.info("hold lock and update  time");
            zeusSchedule.startup(port);
        } else {
            log.info("not my lock");
            long currentTime = System.currentTimeMillis();
            long lockTime = zeusLock.getServerUpdate().getTime();
            long interval = currentTime - lockTime;//host不匹配，切服务器更新时间间隔超过5s,判断发生master  切换
            if (interval > 1000 * 60 * 5L && isPreemptionHost()) {
                log.info("master 发生切换");
            } else {
                zeusSchedule.shutdown();//非主节点，调度器不执行
            }
            try {
                workClient.connect(host, port);
            } catch (Exception e) {
                log.info("client worker connect master server exception");
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
