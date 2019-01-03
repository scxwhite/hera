package com.dfire.core.event.listenter;

import com.dfire.common.constants.Constants;
import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.HeraJobMonitor;
import com.dfire.common.entity.HeraUser;
import com.dfire.common.service.EmailService;
import com.dfire.common.service.HeraJobMonitorService;
import com.dfire.common.service.HeraJobService;
import com.dfire.common.service.HeraUserService;
import com.dfire.common.util.ActionUtil;
import com.dfire.common.util.NamedThreadFactory;
import com.dfire.core.config.HeraGlobalEnvironment;
import com.dfire.core.event.HeraJobFailedEvent;
import com.dfire.core.event.base.MvcEvent;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.logs.ErrorLog;
import com.dfire.logs.ScheduleLog;
import org.apache.commons.lang.StringUtils;

import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 任务失败的预处理
 * @author xiaosuda
 */
public class HeraJobFailListener extends AbstractListener {

    private HeraUserService heraUserService;
    private HeraJobMonitorService heraJobMonitorService;
    private HeraJobService heraJobService;
    private EmailService emailService;
    private Executor executor;
    //告警接口，待开发

    public HeraJobFailListener(MasterContext context) {
        heraUserService = context.getHeraUserService();
        heraJobMonitorService = context.getHeraJobMonitorService();
        emailService = context.getEmailService();
        heraJobService = context.getHeraJobService();
        executor = new ThreadPoolExecutor(
                1, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(Integer.MAX_VALUE), new NamedThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
    }

    @Override
    public void beforeDispatch(MvcEvent mvcEvent) {
        if (mvcEvent.getApplicationEvent() instanceof HeraJobFailedEvent) {
            HeraJobFailedEvent failedEvent = (HeraJobFailedEvent) mvcEvent.getApplicationEvent();
            String actionId = failedEvent.getActionId();
            Integer jobId = ActionUtil.getJobId(actionId);
            if (jobId == null) {
                return;
            }
            HeraJob heraJob = heraJobService.findById(jobId);
            //非开启任务不处理  最好能把这些抽取出去 提供接口实现
            if (heraJob.getAuto() != 1) {
                return ;
            }
            executor.execute(() -> {
                List<String> emails = new ArrayList<>(1);
                try {
                    HeraJobMonitor monitor = heraJobMonitorService.findByJobId(heraJob.getId());
                    if (monitor == null && Constants.PUB_ENV.equals(HeraGlobalEnvironment.getEnv())) {
                        ScheduleLog.info("任务无监控人，发送给owner：{}", heraJob.getId());
                        HeraUser user = heraUserService.findByName(heraJob.getOwner());
                        emails.add(user.getEmail().trim());
                    } else if (monitor != null) {
                        String ids = monitor.getUserIds();
                        String[] id = ids.split(Constants.COMMA);
                        for (String anId : id) {
                            if (StringUtils.isBlank(anId)) {
                                continue;
                            }
                            HeraUser user = heraUserService.findById(Integer.parseInt(anId));
                            if (user != null && user.getEmail() != null) {
                                emails.add(user.getEmail());
                            }
                        }
                    }
                    if (emails.size() > 0) {
                        emailService.sendEmail("hera任务失败了(" + HeraGlobalEnvironment.getEnv() + ")", "任务Id :" + actionId, emails);
                    }
                } catch (MessagingException e) {
                    e.printStackTrace();
                    ErrorLog.error("发送邮件失败");
                }
            });


        }
    }
}
