package com.dfire.event;

import com.dfire.common.entity.HeraDebugHistory;
import lombok.Builder;
import lombok.Getter;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:06 2018/4/19
 * @desc
 */
@Builder
public class HeraDebugSuccessEvent extends ApplicationEvent {

    @Getter
    private HeraDebugHistory history;
    private Integer fileId;

    public HeraDebugSuccessEvent(HeraDebugHistory history, Integer fileId) {
        super(Events.JobSucceed);
        this.fileId = fileId;
        this.history = history;
    }

}
