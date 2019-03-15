package com.dfire.event;

import com.dfire.common.enums.TriggerTypeEnum;
import lombok.Data;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:27 2018/4/19
 * @desc
 */
@Data
public class HeraJobSuccessEvent extends ApplicationEvent {

    private String historyId;
    private String jobId;
    private String statisticEndTime;
    private TriggerTypeEnum triggerType;

    public HeraJobSuccessEvent(String jobId, TriggerTypeEnum triggerType, String historyId) {
        super(Events.JobSucceed);
        this.jobId = jobId;
        this.triggerType = triggerType;
        this.historyId = historyId;
    }

}
