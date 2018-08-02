package com.dfire.common.service;

import com.dfire.common.entity.HeraJobMonitor;

/**
 *
 * @author xiaosuda
 * @date 2018/8/1
 */
public interface HeraJobMonitorService {


    boolean addMonitor(String userId, Integer jobId);

    boolean removeMonitor(String userId, Integer jobId);


    HeraJobMonitor findByJobId(Integer jobId);
}
