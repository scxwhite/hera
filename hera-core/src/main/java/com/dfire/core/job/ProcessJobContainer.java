package com.dfire.core.job;

import com.dfire.common.constants.Constants;
import com.dfire.common.constants.RunningJobKeyConstant;
import com.dfire.common.exception.HeraException;
import com.dfire.config.HeraGlobalEnv;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午12:30 2018/4/26
 * @desc job执行单元的集合体，按照处理顺序，先执行前置处理（job执行所需进行的upload,download），
 * 在执行核心处理（shell, hive）(脚本执行逻辑),最后执行后置处理
 */

public class ProcessJobContainer extends AbstractJob {


    private List<Job> pres;
    private List<Job> posts;
    private Job job;

    private Job running;


    public ProcessJobContainer(JobContext jobContext, List<Job> pres, List<Job> posts, Job core) {
        super(jobContext);
        this.pres = pres;
        this.job = core;
        this.posts = posts;
    }


    /**
     * 单个任务完整执行逻辑，按照前置，core，后置顺序执行
     *
     * @return
     */
    @Override
    public int run() throws Exception {
        int exitCode = -1;
        try {
            if (HeraGlobalEnv.isScriptEcho() || Boolean.parseBoolean(getProperty(Constants.HERA_SCRIPT_ECHO, "false"))) {
                String echoLog = "==================开始输出脚本内容==================\n" +
                        Constants.NEW_LINE + getProperty(RunningJobKeyConstant.JOB_SCRIPT);
                log(echoLog);
                log("==================结束输出脚本内容==================");
            }
            getProperties().setProperty(Constants.EMR_SELECT_WORK, getLoginCmd());
            for (Job job : pres) {
                if (isCanceled()) {
                    break;
                }
                running = job;
                log("开始执行前置处理单元" + job.getClass().getSimpleName());
                jobContext.setPreExitCode(running.run());
                log("前置处理单元" + job.getClass().getSimpleName() + "处理完毕");
                running = null;
            }
            jobContext.setCoreExitCode(exitCode);
            if (!isCanceled()) {
                log("开始执行核心job");
                running = job;
                exitCode = job.run();
                jobContext.setCoreExitCode(exitCode);
            }
            log("核心job处理完毕");
            running = null;

            for (Job job : posts) {
                if (isCanceled()) {
                    break;
                }
                log("开始执行后置处理单元" + job.getClass().getSimpleName());
                running = job;
                jobContext.setPreExitCode(running.run());
                log("后置置处理单元" + job.getClass().getSimpleName() + "处理完毕");
                running = null;
            }
        } catch (Exception e) {
            log(e);
            throw new HeraException("执行任务异常:", e);
        } finally {
            log("exitCode = " + exitCode);
        }

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
