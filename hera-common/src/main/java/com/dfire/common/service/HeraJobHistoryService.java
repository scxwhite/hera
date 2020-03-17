package com.dfire.common.service;

import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.entity.vo.PageHelper;
import com.dfire.common.entity.vo.PageHelperTimeRange;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 19:18 2018/1/12
 * @desc
 */
public interface HeraJobHistoryService {


    int updateHeraJobHistoryStatus(HeraJobHistory jobStatus);

    int updateHeraJobHistoryLog(HeraJobHistory heraJobHistory);

    int insert(HeraJobHistory heraJobHistory);

    int delete(Long id);

    int update(HeraJobHistory heraJobHistory);

    int updateStatusAndIllustrate(Long id, String status, String illustrate, Date endTime);

    List<HeraJobHistory> getAll();

    HeraJobHistory findById(Long id);

    List<HeraJobHistory> findByActionId(Long actionId);

    Integer updateHeraJobHistoryLogAndStatus(HeraJobHistory build);

    /**
     * 根据jobId查询运行历史
     *
     * @param jobId
     * @return
     */
    List<HeraJobHistory> findByJobId(Long jobId);

    HeraJobHistory findLogById(Integer id);

    Map<String, Object> findLogByPage(PageHelperTimeRange pageHelperTimeRange);

    List<HeraJobHistory> findTodayJobHistory();

    void deleteHistoryRecord(Integer beforeDay);

    HeraJobHistory findNewest(Long jobId);

    HeraJobHistory findPropertiesById(Long id);
}
