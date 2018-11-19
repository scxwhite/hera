package com.dfire.common.service.impl;

import com.dfire.common.constants.Constants;
import com.dfire.common.entity.HeraAction;
import com.dfire.common.entity.vo.HeraActionVo;
import com.dfire.common.kv.Tuple;
import com.dfire.common.mapper.HeraJobActionMapper;
import com.dfire.common.service.HeraJobActionService;
import com.dfire.common.util.BeanConvertUtils;
import com.dfire.common.util.ActionUtil;
import com.dfire.common.vo.JobStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午3:43 2018/5/16
 * @desc
 */
@Service("heraJobActionService")
public class HeraJobActionServiceImpl implements HeraJobActionService {

    @Autowired
    private HeraJobActionMapper heraJobActionMapper;


    private static Set<String> set = new HashSet<>();

    @Override
    public List<HeraAction> batchInsert(List<HeraAction> heraActionList) {
        System.out.println("batch size is :" + heraActionList.size());
        List<HeraAction> insertList = new ArrayList<>();
        List<HeraAction> updateList = new ArrayList<>();
        int i = 0;
        for(HeraAction heraAction : heraActionList){
            if (isNeedUpdateAction(heraAction)) {
                updateList.add(heraAction);
            } else {
                insertList.add(heraAction);
            }
        }
        if(insertList.size() != 0){

            heraJobActionMapper.batchInsert(insertList);
        }
        if(updateList.size() != 0 ){
            Set<String> sets = new HashSet<>();
            for (HeraAction heraAction : updateList) {
                heraJobActionMapper.update(heraAction);
            }

//            heraJobActionMapper.batchUpdate(updateList);
        }
        return heraActionList;
    }

    /**
     * 判断是更新该是修改
     * @param heraAction
     * @return
     */
    private boolean isNeedUpdateAction(HeraAction heraAction){
        HeraAction action = heraJobActionMapper.findById(heraAction);
        if (action != null) {
            //如果该任务不是在运行中
            if (!Constants.STATUS_RUNNING.equals(action.getStatus())) {
                heraAction.setStatus(action.getStatus());
                heraAction.setHistoryId(action.getHistoryId());
                heraAction.setReadyDependency(action.getReadyDependency());
                heraAction.setGmtCreate(action.getGmtCreate());
            } else {
                heraAction = action;
                heraAction.setGmtModified(new Date());
            }
            return true;
        }
        return false;

    }

    @Override
    public int insert(HeraAction heraAction) {
        if (isNeedUpdateAction(heraAction)) {
            return heraJobActionMapper.update(heraAction);
        }else {
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
        HeraAction heraAction = HeraAction.builder().jobId(jobId).build();
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

    @Override
    public Integer updateStatusAndReadDependency(HeraAction heraAction) {
        return heraJobActionMapper.updateStatusAndReadDependency(heraAction);
    }

    @Override
    public List<HeraAction> getTodayAction() {
        return heraJobActionMapper.selectTodayAction(ActionUtil.getInitActionVersion());
    }

}
