package com.dfire.core.event;

import com.dfire.common.enums.TriggerTypeEnum;
import com.dfire.common.entity.vo.HeraJobHistoryVo;
import com.dfire.core.event.base.ApplicationEvent;
import com.dfire.core.event.base.Events;
import lombok.Data;

import java.util.Objects;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:11 2018/4/19
 * @desc
 */
@Data
public class HeraJobFailedEvent extends ApplicationEvent {

    private final HeraJobHistoryVo heraJobHistory;
    private final String actionId;
    private final TriggerTypeEnum triggerType;
    private int runCount = 0;
    private int rollBackTime = 0;

    public HeraJobFailedEvent(String jobId, TriggerTypeEnum triggerType, HeraJobHistoryVo heraJobHistory) {
        super(Events.JobFailed);
        this.actionId = jobId;
        this.triggerType = triggerType;
        this.heraJobHistory = heraJobHistory;
    }

    public void setRollBackTime(int value) {
        if (Objects.equals(triggerType, TriggerTypeEnum.SCHEDULE) || Objects.equals(triggerType, TriggerTypeEnum.MANUAL_RECOVER)) {
            this.rollBackTime = value;
        }
    }



}
