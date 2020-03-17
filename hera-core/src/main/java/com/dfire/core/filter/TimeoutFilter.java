package com.dfire.core.filter;

import com.dfire.common.config.ExecuteFilter;
import com.dfire.common.config.Filter;
import com.dfire.common.config.ServiceLoader;
import com.dfire.common.constants.Constants;
import com.dfire.common.entity.HeraJobMonitor;
import com.dfire.common.entity.HeraSso;
import com.dfire.common.service.HeraJobMonitorService;
import com.dfire.common.service.HeraSsoService;
import com.dfire.common.service.JobFailAlarm;
import com.dfire.common.util.ActionUtil;
import com.dfire.common.util.NamedThreadFactory;
import com.dfire.common.vo.JobElement;
import com.dfire.config.HeraGlobalEnv;
import com.dfire.logs.ErrorLog;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * desc:
 *
 * @author scx
 * @create 2019/05/08
 */
@Filter("timeoutFilter")
public class TimeoutFilter implements ExecuteFilter {

    @Autowired
    private HeraJobMonitorService jobMonitorService;

    @Autowired
    private HeraSsoService heraSsoService;


    private volatile Timer timeoutCheck;

    private ConcurrentHashMap<JobElement, Timeout> cache;

    @Override
    public void onExecute(JobElement jobElement) {
        if (jobElement.getCostMinute() == null || jobElement.getCostMinute() <= 0) {
            return;
        }
        if (timeoutCheck == null) {
            synchronized (this) {
                if (timeoutCheck == null) {
                    timeoutCheck = new HashedWheelTimer(
                            new NamedThreadFactory("timeout-check-timer", true),
                            1,
                            TimeUnit.SECONDS);
                    cache = new ConcurrentHashMap<>(HeraGlobalEnv.getMaxParallelNum());
                }
            }
        }
        cache.putIfAbsent(jobElement, timeoutCheck.newTimeout(timeout -> {
            List<JobFailAlarm> alarms = ServiceLoader.getAlarms();
            Set<HeraSso> monitorUser = getMonitorUser(ActionUtil.getJobId(jobElement.getJobId()));
            for (JobFailAlarm alarm : alarms) {
                try {
                    alarm.alarm(jobElement, monitorUser);
                } catch (Exception e) {
                    ErrorLog.error("告警失败:", e);
                }
            }
        }, jobElement.getCostMinute(), TimeUnit.MINUTES));
    }

    @Override
    public void onResponse(JobElement element) {
        if (cache != null) {
            Timeout remove = cache.remove(element);
            if (remove != null) {
                remove.cancel();
            }
        }
    }

    private Set<HeraSso> getMonitorUser(Integer jobId) {
        Set<HeraSso> monitorUser = new HashSet<>();
        Optional.ofNullable(jobMonitorService.findByJobId(jobId))
                .map(HeraJobMonitor::getUserIds)
                .ifPresent(ids -> Arrays.stream(ids
                        .split(Constants.COMMA))
                        .filter(StringUtils::isNotBlank)
                        .forEach(id -> {
                            Optional.ofNullable(heraSsoService.findSsoById(Integer.parseInt(id))).ifPresent(monitorUser::add);
                        }));
        return monitorUser;
    }
}
