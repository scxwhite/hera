package com.dfire.common.entity.vo;

import lombok.Data;

/**
 *
 * @author xiaosuda
 * @date 2019/1/24
 */
@Data
public class JobLogHistory {

    private String id;

    private String actionId;

    private String endTime;

    private String startTime;

    private int jobId;

    private String status;

    private String executeHost;

    private String operator;

    private String illustrate;

    private int triggerType;
    
    private String batchId;

    private String bizLabel;

}
