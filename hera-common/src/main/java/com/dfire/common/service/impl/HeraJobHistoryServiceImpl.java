package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.entity.vo.JobLogHistory;
import com.dfire.common.entity.vo.PageHelper;
import com.dfire.common.mapper.HeraJobHistoryMapper;
import com.dfire.common.service.HeraJobHistoryService;
import com.dfire.common.util.ActionUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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
    public int updateHeraJobHistoryLog(HeraJobHistory heraJobHistory) {
        return heraJobHistoryMapper.updateHeraJobHistoryLog(heraJobHistory);
    }

    @Override
    public int updateHeraJobHistoryStatus(HeraJobHistory HeraJobHistory) {
        return heraJobHistoryMapper.updateHeraJobHistoryStatus(HeraJobHistory);
    }

    @Override
    public int insert(HeraJobHistory heraJobHistory) {
        return heraJobHistoryMapper.insert(heraJobHistory);
    }

    @Override
    public int delete(String id) {
        return heraJobHistoryMapper.delete(id);
    }

    @Override
    public int update(HeraJobHistory heraJobHistory) {
        return heraJobHistoryMapper.update(heraJobHistory);
    }

    @Override
    public int updateStatusAndIllustrate(Integer id, String status, String illustrate, Date endTime) {
        return heraJobHistoryMapper.updateStatusAndIllustrate(id, status, illustrate, endTime);
    }

    @Override
    public List<HeraJobHistory> getAll() {
        return heraJobHistoryMapper.getAll();
    }

    @Override
    public HeraJobHistory findById(String id) {
        return heraJobHistoryMapper.findById(id);
    }

    @Override
    public List<HeraJobHistory> findByActionId(String actionId) {
        return heraJobHistoryMapper.findByActionId(actionId);
    }

    @Override
    public Integer updateHeraJobHistoryLogAndStatus(HeraJobHistory heraJobHistory) {
        return heraJobHistoryMapper.updateHeraJobHistoryLogAndStatus(heraJobHistory);
    }

    @Override
    public List<HeraJobHistory> findByJobId(String jobId) {
        return heraJobHistoryMapper.findByJobId(jobId);
    }

    @Override
    public HeraJobHistory findLogById(Integer id) {
        return heraJobHistoryMapper.selectLogById(id);
    }

    @Override
    public Map<String, Object> findLogByPage(PageHelper pageHelper) {
        Map<String, Object> res = new HashMap<>(2);
        Integer size = heraJobHistoryMapper.selectCountById(pageHelper.getJobId());
        List<HeraJobHistory> histories = heraJobHistoryMapper.selectByPage(pageHelper);
        List<JobLogHistory> jobLogHistories = new ArrayList<>();
        for (HeraJobHistory history : histories) {
            JobLogHistory logHistory = new JobLogHistory();
            BeanUtils.copyProperties(history, logHistory);
            logHistory.setStartTime(ActionUtil.getDefaultFormatterDate(history.getStartTime()));
            logHistory.setEndTime(ActionUtil.getDefaultFormatterDate(history.getEndTime()));
            jobLogHistories.add(logHistory);
        }
        res.put("rows", jobLogHistories);
        res.put("total", size);
        return res;
    }

    @Override
    public List<HeraJobHistory> findTodayJobHistory() {
        return heraJobHistoryMapper.findTodayJobHistory();
    }

    @Override
    public void deleteHistoryRecord(Integer beforeDay) {
        heraJobHistoryMapper.deleteHistoryRecord(beforeDay);
    }

    @Override
    public HeraJobHistory findNewest(String jobId) {
        return heraJobHistoryMapper.findNewest(jobId);
    }


}
