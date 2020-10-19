package com.dfire.common.entity.vo;

import com.dfire.common.enums.StatusEnum;
import com.dfire.common.enums.TriggerTypeEnum;
import com.dfire.common.vo.JobStatus;
import com.dfire.common.vo.LogContent;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午7:21 2018/5/12
 * @desc
 */
@Builder
@Data
public class HeraJobHistoryVo {

    private Long id;

    private Long actionId;

    private Integer jobId;

    private Date startTime;

    private Date endTime;

    private String executeHost;

    private String operator;

    private StatusEnum statusEnum;

    private TriggerTypeEnum triggerType;

    private String illustrate;

    private Date statisticsEndTime;

    private LogContent log;

    private String timezone;

    private String cycle;

    private int hostGroupId;

    private Map<String, String> properties;
    
    private String batchId;
    private String bizLabel;





}
