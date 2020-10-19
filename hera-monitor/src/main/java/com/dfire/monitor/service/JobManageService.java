package com.dfire.monitor.service;


import com.dfire.common.entity.model.JsonResponse;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:12 2018/8/14
 * @desc
 */
public interface JobManageService {


    
    /**
     * 进入任务详情查询
     *
     * @param status
     * @return
     */
    JsonResponse findJobHistoryByStatus(String status, String begindt,String enddt);
    

    /**
     * 查询任务运行时长top10
     *
     * @return
     */
    JsonResponse findJobRunTimeTop10();

    /**
     * 今日所有任务状态，首页饼图
     *
     * @return
     */
    JsonResponse findAllJobStatus();


    /**
     * 今日任务详情明细，初始化曲线图
     *
     * @return
     */

    JsonResponse findAllJobStatusDetail();
}
