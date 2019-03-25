package com.dfire.core.event.listenter;

import com.dfire.event.Events;
import com.dfire.core.event.base.MvcEvent;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午5:39 2018/4/19
 * @desc  取消初始化事件，放置Job进行出错任务重试，以及开启定时器
 */
public class HeraStopScheduleJobListener extends AbstractListener {

    @Override
    public void beforeDispatch(MvcEvent mvcEvent) {
        if (mvcEvent.getApplicationEvent().getType() == Events.Initialize) {
            mvcEvent.setCancelled(true);
        }
    }
}
