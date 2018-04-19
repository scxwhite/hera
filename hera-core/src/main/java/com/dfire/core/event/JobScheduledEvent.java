package com.dfire.core.event;

import lombok.Builder;
import lombok.Data;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 11:25 2018/1/14
 * @desc 自动调度任务事件
 */
@Data
@Builder
public class JobScheduledEvent {

    public final String jobId;

    public JobScheduledEvent(String id) {
        this.jobId = id;
    }


}
