package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.entity.vo.HeraJobHistoryVo;
import com.dfire.common.mapper.HeraJobHistoryMapper;
import com.dfire.common.service.HeraJobHistoryService;
import com.dfire.common.vo.JobStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 19:18 2018/1/12
 * @desc
 */
@Service("heraJobHistoryService")
public class HeraJobHistoryServiceImpl implements HeraJobHistoryService {
    @Autowired
    HeraJobHistoryMapper heraJobHistoryMapper;

    @Override
    public HeraJobHistoryVo findJobHistory(String id) {
        return null;
    }

    @Override
    public void addHeraJobHistory(HeraJobHistory heraJobHistory) {

    }

    @Override
    public void updateHeraJobHistory(HeraJobHistory heraJobHistory) {

    }

    //任务链路的任务状态也需要更新
    @Override
    public void updateJobStatus(JobStatus jobStatus) {

    }
}
