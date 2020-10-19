package com.dfire.common.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.entity.model.TablePageForm;
import com.dfire.common.entity.vo.JobLogHistoryVo;
import com.dfire.common.entity.vo.PageHelperTimeRange;
import com.dfire.common.enums.StatusEnum;
import com.dfire.common.enums.TriggerTypeEnum;
import com.dfire.common.mapper.HeraJobHistoryMapper;
import com.dfire.common.service.HeraJobHistoryService;
import com.dfire.common.service.HeraJobService;
import com.dfire.common.util.ActionUtil;
import com.dfire.common.util.Pair;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Autowired
    @Qualifier("heraJobMemoryService")
    private HeraJobService heraJobService;


    @Override
    public int updateHeraJobHistoryLog(HeraJobHistory heraJobHistory) {
        return heraJobHistoryMapper.updateHeraJobHistoryLog(heraJobHistory);
    }

    @Override
    public int updateHeraJobHistoryStatus(HeraJobHistory herajobhistory) {
        return heraJobHistoryMapper.updateHeraJobHistoryStatus(herajobhistory);
    }

    @Override
    public int insert(HeraJobHistory heraJobHistory) {
        return heraJobHistoryMapper.insert(heraJobHistory);
    }

    @Override
    public int delete(Long id) {
        return heraJobHistoryMapper.delete(id);
    }

    @Override
    public int update(HeraJobHistory heraJobHistory) {
        return heraJobHistoryMapper.update(heraJobHistory);
    }

    @Override
    public int updateProperties(HeraJobHistory heraJobHistory) {
        return heraJobHistoryMapper.updateHeraJobHistoryProperties(heraJobHistory);
    }

    @Override
    public int updateStatusAndIllustrate(Long id, String status, String illustrate, Date endTime) {
        return heraJobHistoryMapper.updateStatusAndIllustrate(id, status, illustrate, endTime);
    }

    @Override
    public List<HeraJobHistory> getAll() {
        return heraJobHistoryMapper.getAll();
    }

    @Override
    public HeraJobHistory findById(Long id) {
        return heraJobHistoryMapper.findById(id);
    }

    @Override
    public List<HeraJobHistory> findByActionId(Long actionId) {
        return heraJobHistoryMapper.findByActionId(actionId);
    }

    @Override
    public Integer updateHeraJobHistoryLogAndStatus(HeraJobHistory heraJobHistory) {
        return heraJobHistoryMapper.updateHeraJobHistoryLogAndStatus(heraJobHistory);
    }

    @Override
    public List<HeraJobHistory> findByJobId(Long jobId) {
        return heraJobHistoryMapper.findByJobId(jobId);
    }

    @Override
    public HeraJobHistory findLogById(Integer id) {
        return heraJobHistoryMapper.selectLogById(id);
    }

    @Override
    public Map<String, Object> findLogByPage(PageHelperTimeRange pageHelperTimeRange) {
        Map<String, Object> res = new HashMap<>(2);
        Integer size;
        List<JobLogHistoryVo> histories;

        if (pageHelperTimeRange.getJobType().equals("job")) {
            size = heraJobHistoryMapper.selectCountByPageJob(pageHelperTimeRange);
            histories = heraJobHistoryMapper.selectByPageJob(pageHelperTimeRange);
        } else {
            size = heraJobHistoryMapper.selectCountByPageGroup(pageHelperTimeRange);
            histories = heraJobHistoryMapper.selectByPageGroup(pageHelperTimeRange);
        }
        histories.stream().filter(his -> StringUtils.isNotBlank(his.getTriggerType())).forEach(his -> his.setTriggerType(TriggerTypeEnum.parser(Integer.parseInt(his.getTriggerType())).toName()));
        res.put("rows", histories);
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
    public HeraJobHistory findNewest(Integer jobId) {
        return heraJobHistoryMapper.findNewest(jobId);
    }


    @Override
    public HeraJobHistory findPropertiesById(Long id) {
        return heraJobHistoryMapper.findPropertiesBy(id);
    }


    @Override
    public Pair<Integer, List<JSONObject>> findRerunFailed(Integer jobId, String rerunId, long actionId, TablePageForm pageForm) {
        List<HeraJobHistory> rerunFailedIds = heraJobHistoryMapper.findRerunFailed(jobId, rerunId, actionId, pageForm.getStartPos(), pageForm.getLimit());
        HeraJob memById = heraJobService.findMemById(jobId);
        List<JSONObject> res = new ArrayList<>();
        for (HeraJobHistory rerunFailedId : rerunFailedIds) {
            JSONObject object = new JSONObject();
            object.put("actionId", String.valueOf(rerunFailedId.getActionId()));
            object.put("name", memById.getName());
            object.put("startTime", ActionUtil.getDefaultFormatterDate(rerunFailedId.getStartTime()));
            object.put("endTime", ActionUtil.getDefaultFormatterDate(rerunFailedId.getEndTime()));
            object.put("status", StatusEnum.FAILED.toString());
            res.add(object);
        }
        return Pair.of(heraJobHistoryMapper.findRerunFailedCount(jobId, rerunId, actionId), res);
    }

    @Override
    public List<HeraJobHistory> findRerunFailedIdsByLimit(Long lastId, Integer jobId, String rerunId, Integer limit) {
        return heraJobHistoryMapper.findRerunFailedIdsByLimit(lastId, jobId, rerunId, limit);
    }

    @Override
    public Integer findRerunFailedCount(Integer jobId, String rerunId, long actionId) {
        return heraJobHistoryMapper.findRerunFailedCount(jobId, rerunId, actionId);
    }


}
