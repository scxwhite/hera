package com.dfire.common.service;

import com.dfire.common.entity.HeraJobMonitor;
import com.dfire.common.entity.vo.HeraJobMonitorVo;

import java.util.List;

/**
 * @author xiaosuda
 * @date 2018/8/1
 */
public interface HeraJobMonitorService {


    boolean addMonitor(String userId, Integer jobId);

    boolean removeMonitor(String userId, Integer jobId);

    HeraJobMonitor findByJobId(Integer jobId);

    List<HeraJobMonitor> findAll();

    List<HeraJobMonitorVo> findAllVo();

    boolean updateMonitor(String userIds, Integer jobId);
}
