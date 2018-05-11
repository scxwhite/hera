package com.dfire.common.service;

import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.vo.JobStatus;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 19:18 2018/1/12
 * @desc
 */
public interface HeraJobHistoryService {

    public HeraJobHistory findJobHistory(String id);

    public void addHeraJobHistory(HeraJobHistory heraJobHistory);

    public void updateHeraJobHistory(HeraJobHistory heraJobHistory);

    public void updateJobStatus(JobStatus jobStatus);


}
