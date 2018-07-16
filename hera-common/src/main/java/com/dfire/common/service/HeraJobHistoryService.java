package com.dfire.common.service;

import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.entity.vo.HeraJobHistoryVo;
import com.dfire.common.vo.JobStatus;
import com.dfire.common.vo.LogContent;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 19:18 2018/1/12
 * @desc
 */
public interface HeraJobHistoryService {


    int updateHeraJobHistoryStatus(HeraJobHistory jobStatus);

    int updateHeraJobHistoryLog(HeraJobHistory heraJobHistory);

    int insert(HeraJobHistory heraJobHistory);

    int delete(String id);

    int update(HeraJobHistory heraJobHistory);

    List<HeraJobHistory> getAll();

    HeraJobHistory findById(String id);

    HeraJobHistory findByActionId(String actionId);

    Integer updateHeraJobHistoryLogAndStatus(HeraJobHistory build);

    /**
     * 根据jobId查询运行历史
     *
     * @param jobId
     * @return
     */
    List<HeraJobHistory> findByJobId(String jobId);

    HeraJobHistory findLogById(Integer id);
}
