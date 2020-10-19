package com.dfire.core.event.listenter;

import com.dfire.common.enums.TriggerTypeEnum;
import com.dfire.core.event.base.MvcEvent;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.event.HeraJobSuccessEvent;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午5:38 2018/4/19
 * @desc
 */
public class HeraJobSuccessListener extends AbstractListener {


    public HeraJobSuccessListener(MasterContext context) {
    }

    /**
     * 任务成功的前置操作，可以在这里处理任务成功单未通知下游任务的的前置操作，比如：通知数据质量平台某某任务成功，可以进行检测
     * @param mvcEvent
     */
    @Override
    public void beforeDispatch(MvcEvent mvcEvent) {
        if(mvcEvent.getApplicationEvent() instanceof HeraJobSuccessEvent) {
            HeraJobSuccessEvent jobSuccessEvent = (HeraJobSuccessEvent) mvcEvent.getApplicationEvent();
            if(jobSuccessEvent.getTriggerType() == TriggerTypeEnum.SCHEDULE) {
                return;
            }
        }
    }
}
