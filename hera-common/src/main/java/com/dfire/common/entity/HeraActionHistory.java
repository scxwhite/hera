package com.dfire.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 14:13 2017/12/30
 * @desc
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HeraActionHistory {

    private Integer id;
    private Integer jobId;
    private Integer actionId;
    private String cycle;
    private Date endTime;
    private String executeHost;
    private String gmtCreate;
    private Date gmtModified;
    private String illustrate;
    private String log;
    private String operator;
    private String properties;
    private Date startTime;
    private Date statisticEndTime;
    private String status;
    private Integer triggerType;
    private Integer hostGroupId;
}
