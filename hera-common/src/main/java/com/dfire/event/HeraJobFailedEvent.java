package com.dfire.event;

import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.vo.HeraJobHistoryVo;
import com.dfire.common.enums.TriggerTypeEnum;
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
    private final Long actionId;
    private final TriggerTypeEnum triggerType;
    private HeraJob heraJob;
    private int runCount = 0;
    private int rollBackTime = 0;
    private int retryCount = 0;

    public HeraJobFailedEvent(Long jobId, TriggerTypeEnum triggerType, HeraJobHistoryVo heraJobHistory) {
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
