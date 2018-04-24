package com.dfire.core.event;

import com.dfire.common.constant.TriggerType;
import com.dfire.common.entity.HeraJobHistory;
import com.dfire.core.event.base.ApplicationEvent;
import com.dfire.core.event.base.Events;
import lombok.Builder;
import lombok.Data;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:11 2018/4/19
 * @desc
 */
@Data
public class HeraJobFailedEvent extends ApplicationEvent {

    private final HeraJobHistory heraJobHistory;
    private final String jobId;
    private final TriggerType triggerType;
    private final int runCount = 0;
    private int rollBackTime = 0;

    public HeraJobFailedEvent(String jobId, TriggerType triggerType, HeraJobHistory heraJobHistory) {
        super(Events.JobFailed);
        this.jobId = jobId;
        this.triggerType = triggerType;
        this.heraJobHistory = heraJobHistory;
    }

    public void setRollBackTime(int value) {
        if (triggerType.equals(triggerType.SCHEDULE)) {
            this.rollBackTime = value;
        }
    }



}
