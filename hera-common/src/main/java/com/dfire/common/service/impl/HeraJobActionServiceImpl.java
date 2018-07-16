package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraAction;
import com.dfire.common.entity.vo.HeraActionVo;
import com.dfire.common.enums.StatusEnum;
import com.dfire.common.kv.Tuple;
import com.dfire.common.mapper.HeraJobActionMapper;
import com.dfire.common.service.HeraJobActionService;
import com.dfire.common.util.BeanConvertUtils;
import com.dfire.common.util.DateUtil;
import com.dfire.common.vo.JobStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午3:43 2018/5/16
 * @desc
 */
@Service("heraJobActionService")
public class HeraJobActionServiceImpl implements HeraJobActionService {

    @Autowired
    private HeraJobActionMapper heraJobActionMapper;

    @Override
    public int insert(HeraAction heraAction) {
        HeraAction action = heraJobActionMapper.findById(heraAction);
        if (action != null) {
            if (action.getStatus() != null && !StatusEnum.RUNNING.toString().equals(action.getStatus())) {
                heraAction.setStatus(action.getStatus());
                heraAction.setHistoryId(action.getHistoryId());
                heraAction.setReadyDependency(action.getReadyDependency());
            } else {
                action.setAuto(heraAction.getAuto());
                heraAction = action;
            }
            return heraJobActionMapper.update(heraAction);
        } else {
            if (Long.parseLong(heraAction.getId()) < Long.parseLong(DateUtil.getTodayStringForAction())) {
                heraAction.setStatus(StatusEnum.FAILED.toString());
            }
            return heraJobActionMapper.insert(heraAction);
        }
    }

    @Override
    public int delete(String id) {
        return heraJobActionMapper.delete(id);
    }

    @Override
    public int update(HeraAction heraAction) {
        return heraJobActionMapper.update(heraAction);
    }

    @Override
    public List<HeraAction> getAll() {
        return heraJobActionMapper.getAll();
    }

    @Override
    public HeraAction findById(String actionId) {
        HeraAction heraAction = HeraAction.builder().id(actionId).build();
        return heraJobActionMapper.findById(heraAction);
    }

    @Override
    public HeraAction findLatestByJobId(String jobId) {
        HeraAction heraAction = HeraAction.builder().id(jobId).build();
        return heraJobActionMapper.findLatestByJobId(heraAction);
    }

    @Override
    public List<HeraAction> findByJobId(String jobId) {
        HeraAction heraAction = HeraAction.builder().jobId(jobId).build();
        return heraJobActionMapper.findByJobId(heraAction);
    }

    @Override
    public int updateStatus(JobStatus jobStatus) {
        HeraAction heraAction = findById(jobStatus.getActionId());
        heraAction.setGmtModified(new Date());
        HeraAction tmp  = BeanConvertUtils.convert(jobStatus);
        heraAction.setStatus(tmp.getStatus());
        heraAction.setReadyDependency(tmp.getReadyDependency());
        heraAction.setHistoryId(jobStatus.getHistoryId());
        return update(heraAction);
    }

    @Override
    public Tuple<HeraActionVo, JobStatus> findHeraActionVo(String actionId) {
        HeraAction heraActionTmp = findById(actionId);
        if(heraActionTmp == null) {
            return null;
        }
        Tuple<HeraActionVo, JobStatus> tuple = BeanConvertUtils.convert(heraActionTmp);
        return tuple;
    }

    @Override
    public JobStatus findJobStatus(String actionId) {
        Tuple<HeraActionVo, JobStatus> tuple = findHeraActionVo(actionId);
        return tuple.getTarget();
    }

    /**
     * 根据jobId查询版本运行信息，只能是取最新版本信息
     * @param jobId
     * @return
     */
    @Override
    public JobStatus findJobStatusByJobId(String jobId) {
        HeraAction heraAction = findLatestByJobId(jobId);
        return findJobStatus(heraAction.getId());
    }

    @Override
    public Integer updateStatus(HeraAction heraAction) {
        return heraJobActionMapper.updateStatus(heraAction);
    }
}
