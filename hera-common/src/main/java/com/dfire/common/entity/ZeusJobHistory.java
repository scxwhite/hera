package com.dfire.common.entity;

import com.dfire.common.constant.Status;
import com.dfire.common.constant.TriggerType;
import com.dfire.common.vo.LogContent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 17:31 2018/1/11
 * @desc
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ZeusJobHistory {

    private String id;

    private String jobId;

    private String toJobId;

    private Date startTime;

    private Date endTime;

    private String executeHost;

    private String operator;

    private Status status;

    private TriggerType triggerType;

    private String illustrate;

    private String statisticsEndTime;

    private LogContent log = new LogContent();

    private String timezone;

    private String cycle;

    private String hostGroupId;
}
