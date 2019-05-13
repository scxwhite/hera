package com.dfire.monitor.domain;

import lombok.Data;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午10:58 2018/8/15
 * @desc
 */
@Data
public class ActionTime {

    private Integer jobId;

    private String actionId;

    private String jobTime;

    private Integer yesterdayTime;
}
