package com.dfire.core.event;

import com.dfire.common.enums.TriggerType;
import com.dfire.core.event.base.ApplicationEvent;
import com.dfire.core.event.base.Events;
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
    private TriggerType triggerType;

    public HeraJobSuccessEvent(String jobId, TriggerType triggerType, String historyId) {
        super(Events.JobSucceed);
        this.jobId = jobId;
        this.triggerType = triggerType;
        this.historyId = historyId;
    }

}
