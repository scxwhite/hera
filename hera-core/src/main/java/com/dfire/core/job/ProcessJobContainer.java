package com.dfire.core.job;

import com.dfire.common.service.HeraDebugHistoryService;
import com.dfire.common.service.HeraFileService;
import org.springframework.context.ApplicationContext;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午12:30 2018/4/26
 * @desc job执行单元的集合体，按照处理顺序，先执行前置处理（job执行所需进行的upload,download），
 *       在执行核心处理（shell, hive）(脚本执行逻辑),最后执行后置处理
 */

public class ProcessJobContainer extends AbstractJob {


    private List<Job> pres;
    private List<Job> posts;
    private Job job;

    private Job running;

    public ProcessJobContainer(JobContext jobContext, List<Job> pres, List<Job> posts, Job core, ApplicationContext applicationContext) {
        super(jobContext);
        this.pres = pres;
        this.job = core;
        this.posts = posts;
    }


    /**
     * 单个任务完整执行逻辑，按照前置，core，后置顺序执行
     *
     * @return
     * @throws Exception
     */
    @Override
    public int run() throws Exception {
        int preExitCode = -1;
        for (Job job : pres) {
            if (isCanceled()) {
                break;
            }
            running = job;
            log("开始执行前置处理单元" + job.getClass().getSimpleName());
            preExitCode = running.run();
            jobContext.setPreExitCode(preExitCode);
            log("前置处理单元" + job.getClass().getSimpleName() + "处理完毕");
            running = null;
        }

        Integer exitCode = -1;
        jobContext.setCoreExitCode(exitCode);
        if (!isCanceled()) {
            log("开始执行核心job");
            running = job;
            exitCode = job.run();
            jobContext.setCoreExitCode(exitCode);
        }
        log("核心job处理完毕");
        running = null;

        int postExitCode = -1;
        for (Job job : posts) {
            if (isCanceled()) {
                break;
            }
            log("开始执行后置处理单元" + job.getClass().getSimpleName());
            running = job;
            postExitCode = running.run();
            jobContext.setPreExitCode(postExitCode);
            log("后置置处理单元" + job.getClass().getSimpleName() + "处理完毕");
            running = null;
        }
        log("exitCode = " + exitCode);
        return exitCode;
    }

    @Override
    public void cancel() {
        log("cancel job start");
        canceled = true;
        if (running != null) {
            running.cancel();
        }
        log("cancel job end");
    }

}
