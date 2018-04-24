package com.dfire.core.event.handler;

import com.dfire.common.service.HeraJobHistoryService;
import com.dfire.core.event.base.ApplicationEvent;
import com.dfire.core.event.base.Events;
import com.dfire.core.netty.master.Master;
import com.dfire.core.netty.master.MasterContext;
import lombok.Builder;
import lombok.Getter;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午5:24 2018/4/19
 * @desc 任务事件处理器
 */
@Builder
public class JobHandler extends AbstractHandler {

    @Getter
    private final String jobId;
    private HeraJobHistoryService jobHistoryService;

    private Master master;
    private MasterContext masterContext;

    public JobHandler(String jobId, Master master, MasterContext masterContext) {
        this.jobId = jobId;
        this.jobHistoryService = jobHistoryService;
        this.master = master;
        this.masterContext = masterContext;
        registerEventType(Events.Initialize);
    }

    @Override
    public void handleEvent(ApplicationEvent event) {

    }

    @Override
    protected void initialize() {

    }

    @Override
    protected void destory() {

    }
}
