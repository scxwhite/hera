package com.dfire.common.entity;

import lombok.Data;

/**
 *
 * @author xiaosuda
 * @date 2018/8/1
 */
@Data
public class HeraJobMonitor {

    /**
     * 任务Id
     */
    private Integer jobId;
    /**
     * 监控人ID ,分割
     */
    private String userIds;
}
