package com.dfire.event;

import com.dfire.common.entity.vo.HeraJobHistoryVo;
import com.dfire.common.enums.TriggerTypeEnum;
import lombok.Data;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:27 2018/4/19
 * @desc
 */
@Data
public class HeraJobSuccessEvent extends ApplicationEvent {


    private final HeraJobHistoryVo heraJobHistory;

    private String statisticEndTime;
    private Long jobId;
    private TriggerTypeEnum triggerType;

    public HeraJobSuccessEvent(Long jobId, TriggerTypeEnum triggerType, HeraJobHistoryVo heraJobHistory) {
        super(Events.JobSucceed);
        this.jobId = jobId;
        this.triggerType = triggerType;
        this.heraJobHistory = heraJobHistory;
    }

}
