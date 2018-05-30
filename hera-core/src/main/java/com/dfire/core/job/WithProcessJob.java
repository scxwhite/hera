package com.dfire.core.job;

import com.dfire.common.entity.HeraFile;
import com.dfire.common.service.HeraFileService;
import org.springframework.context.ApplicationContext;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午12:30 2018/4/26
 * @desc job
 */

public class WithProcessJob extends AbstractJob {


    private List<Job> pres;
    private List<Job> posts;
    private Job job;
    private HeraFileService fileService;

    private Job running;

    public WithProcessJob(JobContext jobContext, List<Job> pres, List<Job> posts, Job core, ApplicationContext applicationContext) {
        super(jobContext);
        this.pres = pres;
        this.job = core;
        this.posts = posts;
        this.fileService = (HeraFileService) applicationContext.getBean("heraFileService");
    }


    @Override
    public int run() {
        String jobId = null;
        String historyId = null;
        boolean isDebug = false;
        HeraFile heraFile = null;
        if(jobContext.getDebugHistory() != null) {
            isDebug = true;
            heraFile = fileService.findById(jobContext.getDebugHistory().getFileId());
        } else {
            jobId = jobContext.getHeraJobHistory().getJobId();
            historyId = jobContext.getHeraJobHistory().getId();
        }
        Integer preExitCode = 0;
        for(Job job : pres) {
            if(isCanceled()) {
                break;
            }
                try {
                    running = job;
                    log("开始执行前置处理单元" + job.getClass().getSimpleName());
                    preExitCode = running.run();

                } catch (Exception e) {
                    jobContext.setPreExitCode(-1);
                } finally {
                    log("前置处理单元" + job.getClass().getSimpleName() + "处理完毕");
                    running = null;
                }
            }

        Integer exitCode = -1;
        jobContext.setCoreExitCode(exitCode);
        try {
            if(!isCanceled()) {
                log("开始执行核心job");
                running = job;
                exitCode = job.run();
                jobContext.setCoreExitCode(exitCode);
            }
        } catch (Exception e) {
            jobContext.setCoreExitCode(exitCode);
            log(e);
        } finally {
            log("核心job处理完毕");
            running = null;
        }

        for(Job job : posts) {
            if(isCanceled()) {
                break;
            }
                try {
                    log("开始执行后置处理单元" + job.getClass().getSimpleName());
                    running = job;
                    preExitCode = running.run();

                } catch (Exception e) {
                    jobContext.setPreExitCode(-1);
                } finally {
                    log("前置处理单元" + job.getClass().getSimpleName() + "处理完毕");
                    running = null;
                }
            }

        return exitCode;
    }

    @Override
    public void cancel() {
        log("cancel job start");
        canceled = true;
        if(running != null) {
            running.cancel();
        }
        log("cancel job end");
    }

}
