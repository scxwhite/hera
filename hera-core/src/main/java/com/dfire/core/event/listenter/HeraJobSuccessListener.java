package com.dfire.core.event.listenter;

import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.enums.TriggerTypeEnum;
import com.dfire.common.entity.vo.HeraJobHistoryVo;
import com.dfire.common.service.HeraGroupService;
import com.dfire.common.service.HeraJobHistoryService;
import com.dfire.core.event.HeraJobSuccessEvent;
import com.dfire.core.event.base.MvcEvent;
import com.dfire.core.netty.master.MasterContext;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午5:38 2018/4/19
 * @desc
 */
public class HeraJobSuccessListener extends AbstractListener {

    private HeraGroupService heraGroupService;
    private HeraJobHistoryService heraJobHistoryService;

    public HeraJobSuccessListener(MasterContext context) {
        heraGroupService = context.getHeraGroupService();
        heraJobHistoryService = context.getHeraJobHistoryService();
    }

    @Override
    public void beforeDispatch(MvcEvent mvcEvent) {
        if(mvcEvent.getApplicationEvent() instanceof HeraJobSuccessEvent) {
            HeraJobSuccessEvent jobSuccessEvent = (HeraJobSuccessEvent) mvcEvent.getApplicationEvent();
            if(jobSuccessEvent.getTriggerType() == TriggerTypeEnum.SCHEDULE) {
                return;
            }
            HeraJobHistory heraJobHistory = heraJobHistoryService.findById(jobSuccessEvent.getHistoryId());
        }
    }
}
