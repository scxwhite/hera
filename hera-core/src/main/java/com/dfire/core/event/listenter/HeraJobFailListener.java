package com.dfire.core.event.listenter;

import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.service.HeraGroupService;
import com.dfire.common.service.HeraJobHistoryService;
import com.dfire.common.service.HeraJobService;
import com.dfire.common.service.HeraUserService;
import com.dfire.common.util.NamedThreadFactory;
import com.dfire.core.event.HeraJobFailedEvent;
import com.dfire.core.event.base.MvcEvent;
import com.dfire.core.netty.master.MasterContext;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午5:38 2018/4/19
 * @desc 任务失败的监听, 当任务失败，需要发送邮件给相关人员
 */
public class HeraJobFailListener extends AbstractListener {

    private HeraJobHistoryService heraJobHistoryService;
    private HeraJobService heraJobService;
    private HeraUserService heraUserService;
    private HeraGroupService heraGroupService;

    //告警接口，待开发

    public HeraJobFailListener(MasterContext context) {
        heraJobHistoryService = context.getHeraJobHistoryService();
        heraJobService = context.getHeraJobService();
        heraUserService = context.getHeraUserService();
        heraGroupService = context.getHeraGroupService();
    }


    @Override
    public void beforeDispatch(MvcEvent mvcEvent) {
        if (mvcEvent.getApplicationEvent() instanceof HeraJobFailedEvent) {
            HeraJobFailedEvent failedEvent = (HeraJobFailedEvent) mvcEvent.getApplicationEvent();
            String jobId = failedEvent.getActionId();
            HeraJobHistory heraJobHistory = heraJobHistoryService.findById(jobId);

            HeraJob heraJob = heraJobService.findById(Integer.parseInt(jobId));
            Executor executor = new ThreadPoolExecutor(
                    1, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(Integer.MAX_VALUE), new NamedThreadFactory(), new ThreadPoolExecutor.AbortPolicy());

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(6000);
                        StringBuffer sb = new StringBuffer();
                        sb.append("Job任务(").append(jobId).append(")").append(heraJob.getName()).append("运行失败");
                        sb.append("<br/>");
                        String config = heraJob.getConfigs();
                        //TODO 任务失败逻辑，执行告警，短信告警，待开发

                    } catch (Exception e) {
                    }
                }
            });


            executor.execute(new Runnable() {
                @Override
                public void run() {
                    System.out.println("任务失败逻辑，执行告警，短信告警，待开发");
                }
            });

        }
    }
}
