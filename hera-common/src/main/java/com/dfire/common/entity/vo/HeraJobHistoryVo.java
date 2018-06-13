package com.dfire.common.entity.vo;

import com.dfire.common.enums.Status;
import com.dfire.common.enums.TriggerType;
import com.dfire.common.vo.LogContent;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午7:21 2018/5/12
 * @desc
 */
@Builder
@Data
public class HeraJobHistoryVo {

    private String id;

    private String actionId;

    private String jobId;

    private Date startTime;

    private Date endTime;

    private String executeHost;

    private String operator;

    private Status status;

    private TriggerType triggerType;

    private String illustrate;

    private Date statisticsEndTime;

    private LogContent log;

    private String timezone;

    private String cycle;

    private int hostGroupId;

    Map<String, String> properties;

}
