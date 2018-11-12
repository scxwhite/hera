package com.dfire.core.lock;

import com.dfire.common.entity.HeraLock;
import com.dfire.common.service.HeraHostRelationService;
import com.dfire.common.service.HeraLockService;
import com.dfire.core.netty.worker.WorkClient;
import com.dfire.core.netty.worker.WorkContext;
import com.dfire.core.schedule.HeraSchedule;
import com.dfire.core.util.NetUtils;
import com.dfire.logs.HeraLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 20:47 2018/1/10
 * @desc 基于数据库实现的分布式锁方案，后面优化成基于redis实现分布式锁
 */
@Component
public class DistributeLock {


    @Autowired
    private HeraHostRelationService hostGroupService;
    @Autowired
    private HeraLockService heraLockService;
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private WorkClient workClient;

    @Autowired
    private HeraSchedule heraSchedule;

    private final long timeout = 1000 * 60 * 5L;

    @PostConstruct
    public void init() {

        workClient.workSchedule.scheduleWithFixedDelay(() -> {
            try {
                checkLock();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 10, 60, TimeUnit.SECONDS);
    }

    public void checkLock() {
        HeraLock heraLock = heraLockService.findById("online");
        if (heraLock == null) {
            heraLock = HeraLock.builder()
                    .host(WorkContext.host)
                    .serverUpdate(new Date())
                    .build();
            heraLockService.insert(heraLock);
        }

        if (WorkContext.host.equals(heraLock.getHost().trim())) {
            heraLock.setServerUpdate(new Date());
            heraLockService.update(heraLock);
            HeraLog.info("hold lock and update time");
            heraSchedule.startup();
        } else {
            long currentTime = System.currentTimeMillis();
            long lockTime = heraLock.getServerUpdate().getTime();
            long interval = currentTime - lockTime;
            if (interval > timeout && isPreemptionHost()) {
                Date date = new Date();
                Integer lock = heraLockService.changeLock(WorkContext.host, date, date, heraLock.getHost());
                if (lock != null && lock > 0) {
                    HeraLog.error("master 发生切换,{} 抢占成功", WorkContext.host);
                    heraSchedule.startup();
                    heraLock.setHost(WorkContext.host);
                    //TODO  接入master切换通知

                } else {
                    HeraLog.info("master抢占失败，由其它worker抢占成功");
                }
            } else {
                //非主节点，调度器不执行
                heraSchedule.shutdown();
            }
        }
        //TODO  是否考虑master不执行work服务
        workClient.init(applicationContext);

        try {
            workClient.connect(heraLock.getHost());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isPreemptionHost() {
        List<String> preemptionHostList = hostGroupService.findPreemptionGroup(1);
        if (preemptionHostList.contains(WorkContext.host)) {
            return true;
        } else {
            HeraLog.info(WorkContext.host + " is not in master group " + preemptionHostList.toString());
            return false;
        }
    }
}