package com.dfire.core.event.listenter;


import com.dfire.common.entity.HeraDebugHistory;
import com.dfire.common.entity.HeraFile;
import com.dfire.common.service.HeraFileService;
import com.dfire.core.event.HeraDebugFailEvent;
import com.dfire.core.event.HeraDebugSuccessEvent;
import com.dfire.core.event.base.MvcEvent;
import com.dfire.core.netty.master.MasterContext;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午5:37 2018/4/19
 * @desc
 */
public class HeraDebugListener extends AbstractListener {

    private HeraFileService heraFileService;

    public HeraDebugListener(MasterContext masterContext) {
        heraFileService = masterContext.getHeraFileService();
    }

    @Override
    public void beforeDispatch(MvcEvent mvcEvent) {
        if(mvcEvent.getApplicationEvent() instanceof HeraDebugFailEvent) {
            HeraDebugFailEvent event = (HeraDebugFailEvent) mvcEvent.getApplicationEvent();
            final HeraDebugHistory history = event.getDebugHistory();
            HeraFile heraFile = heraFileService.getHeraFile(history.getId());
            String msg = "手动调试任务" + heraFile.getName() + "运行失败";


        } else if(mvcEvent.getApplicationEvent() instanceof HeraDebugSuccessEvent) {
            HeraDebugSuccessEvent event = (HeraDebugSuccessEvent) mvcEvent.getApplicationEvent();
            final HeraDebugHistory history = event.getHistory();
            HeraFile heraFile = heraFileService.getHeraFile(history.getId());
            String msg = "手动调试任务" + heraFile.getName() + "运行成功";

        }
    }
}
