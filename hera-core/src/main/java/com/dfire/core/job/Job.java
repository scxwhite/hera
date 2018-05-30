package com.dfire.core.job;


/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 11:35 2018/1/10
 * @desc 统一job类型接口
 */
public interface Job {

    int run() throws Exception;

    void cancel();

    boolean isCanceled();

    JobContext getJobContext();


}
