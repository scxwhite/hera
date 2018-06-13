package com.dfire.core.event.listenter;


import com.dfire.core.event.HeraJobMaintenanceEvent;
import com.dfire.core.event.base.ApplicationEvent;
import com.dfire.core.event.base.Events;
import com.dfire.core.event.base.MvcEvent;
import com.dfire.core.event.handler.AbstractHandler;
import com.dfire.core.event.handler.JobHandler;
import com.dfire.core.netty.master.Master;
import com.dfire.core.netty.master.MasterContext;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午5:19 2018/4/19
 * @desc 增操作，添加controller
 */
@Slf4j
public class HeraAddJobListener extends AbstractListener {

    private Master master;
    private MasterContext masterContext;

    public HeraAddJobListener(Master master, MasterContext masterContext) {
        this.master = master;
        this.masterContext = masterContext;
    }

    @Override
    public void beforeDispatch(MvcEvent mvcEvent) {
        if (mvcEvent.getApplicationEvent() instanceof HeraJobMaintenanceEvent) {
            HeraJobMaintenanceEvent maintenanceEvent = (HeraJobMaintenanceEvent) mvcEvent.getApplicationEvent();
            if (mvcEvent.getType() != Events.UpdateActions) {
                String jobId = maintenanceEvent.getId();
                boolean exist = false;
                for (AbstractHandler handler : masterContext.getDispatcher().getJobHandlers()) {
                    if (handler instanceof JobHandler) {
                        JobHandler jobHandler = (JobHandler) handler;
                        if (jobHandler.getActionId().equals(jobId)) {
                            exist = true;
                            break;
                        }
                    }
                }
                if (!exist) {
                    JobHandler handler = JobHandler.builder()
                            .actionId(jobId)
                            .master(master)
                            .masterContext(masterContext)
                            .build();
                    masterContext.getDispatcher().addJobHandler(handler);
                    handler.handleEvent(new ApplicationEvent(Events.Initialize));
                    mvcEvent.setCancelled(true);
                    log.info("schedule add job with jobId:" + jobId);
                }
            }

        }

    }
}
