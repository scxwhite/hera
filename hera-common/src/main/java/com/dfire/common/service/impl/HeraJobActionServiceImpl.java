package com.dfire.common.service.impl;

import com.dfire.common.constants.Constants;
import com.dfire.common.entity.HeraAction;
import com.dfire.common.entity.model.TablePageForm;
import com.dfire.common.entity.vo.HeraActionMani;
import com.dfire.common.entity.vo.HeraActionVo;
import com.dfire.common.enums.StatusEnum;
import com.dfire.common.kv.Tuple;
import com.dfire.common.mapper.HeraJobActionMapper;
import com.dfire.common.service.HeraJobActionService;
import com.dfire.common.service.HeraJobHistoryService;
import com.dfire.common.service.HeraJobService;
import com.dfire.common.util.ActionUtil;
import com.dfire.common.util.BeanConvertUtils;
import com.dfire.common.vo.GroupTaskVo;
import com.dfire.common.vo.JobStatus;
import com.dfire.logs.HeraLog;
import com.dfire.logs.ScheduleLog;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Autowired
    @Qualifier("heraJobMemoryService")
    private HeraJobService heraJobService;

    @Autowired
    private HeraJobHistoryService heraJobHistoryService;


    @Override
    public List<HeraAction> batchInsert(List<HeraAction> heraActionList, Long nowAction) {
        ScheduleLog.info("batchInsert-> batch size is :{}", heraActionList.size());
        List<HeraAction> insertList = new ArrayList<>();
        for (HeraAction heraAction : heraActionList) {
            //更新时单条更新
            //原因：批量更新时，在极端情况下，数据会有批量数据处理时间的buff
            //     此时如果有其它地方修改了某条数据 会数据库中的数据被批量更新的覆盖
            if (isNeedUpdateAction(heraAction, nowAction)) {
                update(heraAction);
            } else {
                insertList.add(heraAction);
            }
        }
        if (insertList.size() != 0) {
            heraJobActionMapper.batchInsert(insertList);
        }
        return heraActionList;
    }

    /**
     * 判断是更新该是修改
     *
     * @param heraAction
     * @return
     */
    private boolean isNeedUpdateAction(HeraAction heraAction, Long nowAction) {
        HeraAction action = heraJobActionMapper.findById(heraAction.getId());
        if (action != null) {
            //如果该任务不是在运行中
            if (!StatusEnum.RUNNING.toString().equals(action.getStatus())) {
                heraAction.setStatus(action.getStatus());
                heraAction.setHistoryId(action.getHistoryId());
                heraAction.setReadyDependency(action.getReadyDependency());
                heraAction.setGmtCreate(action.getGmtCreate());
            } else {
                BeanUtils.copyProperties(action, heraAction);
            }
            heraAction.setGmtModified(new Date());
            return true;
        } else {
            if (heraAction.getId() < nowAction) {
                heraAction.setStatus(StatusEnum.FAILED.toString());
                heraAction.setLastResult("生成action时，任务过时，直接设置为失败");
                HeraLog.info("生成action时，任务过时，直接设置为失败:" + heraAction.getId());
            }
        }
        return false;

    }

    @Override
    public int insert(HeraAction heraAction, Long nowAction) {
        if (isNeedUpdateAction(heraAction, nowAction)) {
            return heraJobActionMapper.update(heraAction);
        } else {
            return heraJobActionMapper.insert(heraAction);
        }
    }

    @Override
    public int delete(Long id) {
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
    public HeraAction findById(Long actionId) {
        return heraJobActionMapper.findById(actionId);
    }

    @Override
    public HeraAction findById(String actionId) {
        return this.findById(Long.parseLong(actionId));
    }

    @Override
    public HeraAction findLatestByJobId(Long jobId) {
        return heraJobActionMapper.findLatestByJobId(jobId);
    }

    @Override
    public List<HeraAction> findByJobId(Long jobId) {
        return heraJobActionMapper.findByJobId(jobId);
    }

    @Override
    public int updateStatus(JobStatus jobStatus) {
        HeraAction heraAction = findById(jobStatus.getActionId());
        heraAction.setGmtModified(new Date());
        HeraAction tmp = BeanConvertUtils.convert(jobStatus);
        heraAction.setStatus(tmp.getStatus());
        heraAction.setReadyDependency(tmp.getReadyDependency());
        heraAction.setHistoryId(jobStatus.getHistoryId());
        return update(heraAction);
    }

    @Override
    public Tuple<HeraActionVo, JobStatus> findHeraActionVo(Long actionId) {
        HeraAction heraActionTmp = findById(actionId);
        if (heraActionTmp == null) {
            return null;
        }
        return BeanConvertUtils.convert(heraActionTmp);
    }

    @Override
    public JobStatus findJobStatus(Long actionId) {
        Tuple<HeraActionVo, JobStatus> tuple = findHeraActionVo(actionId);
        return tuple.getTarget();
    }

    /**
     * 根据jobId查询版本运行信息，只能是取最新版本信息
     *
     * @param jobId
     * @return
     */
    @Override
    public JobStatus findJobStatusByJobId(Long jobId) {
        HeraAction heraAction = findLatestByJobId(jobId);
        return findJobStatus(heraAction.getId());
    }

    @Override
    public Integer updateStatus(Long id, String status) {
        return heraJobActionMapper.updateStatus(id, status);
    }

    @Override
    public Integer updateStatusAndReadDependency(HeraAction heraAction) {
        return heraJobActionMapper.updateStatusAndReadDependency(heraAction);
    }

    @Override
    public List<HeraAction> getAfterAction(Long action) {
        return heraJobActionMapper.selectAfterAction(action);
    }

    @Override
    public List<Long> getActionVersionByJobId(Long jobId) {
        return heraJobActionMapper.getActionVersionByJobId(jobId);
    }

    @Override
    public List<HeraActionVo> getNotRunScheduleJob() {
        return heraJobActionMapper.getNotRunScheduleJob();
    }

    @Override
    public List<HeraActionVo> getFailedJob() {
        return heraJobActionMapper.getFailedJob();
    }


    @Override
    public List<GroupTaskVo> findByJobIds(List<Integer> idList, String startDate, String endDate, TablePageForm pageForm, String status) {
        if (idList == null || idList.size() == 0) {
            return null;
        }
        Map<String, Object> params = new HashMap<>(3);

        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("list", idList);
        params.put("page", pageForm.getStartPos());
        params.put("limit", pageForm.getLimit());
        List<HeraAction> actionList;
        if (StringUtils.isBlank(status) || "all".equals(status)) {
            params.put("status", null);
        } else {
            params.put("status", status);
        }

        pageForm.setCount(heraJobActionMapper.findByJobIdsCount(params));
        actionList = heraJobActionMapper.findByJobIdsAndPage(params);
        List<GroupTaskVo> res = new ArrayList<>(actionList.size());
        actionList.forEach(action -> {
            GroupTaskVo taskVo = new GroupTaskVo();
            taskVo.setActionId(buildFont(String.valueOf(action.getId()), Constants.STATUS_NONE));
            taskVo.setJobId(buildFont(String.valueOf(action.getJobId()), Constants.STATUS_NONE));
            taskVo.setName(buildFont(action.getName(), Constants.STATUS_NONE));

            if (action.getStatus() != null) {
                taskVo.setStatus(buildFont(action.getStatus(), action.getStatus()));
            } else {
                taskVo.setStatus(buildFont("未执行", StatusEnum.FAILED.toString()));
            }
            taskVo.setLastResult(buildFont(action.getLastResult(), action.getLastResult()));
            if (action.getScheduleType() == 0) {
                taskVo.setReadyStatus(buildFont("独立任务", Constants.STATUS_NONE));
            } else {
                String[] dependencies = action.getDependencies().split(Constants.COMMA);
                StringBuilder builder = new StringBuilder();
                HeraAction heraAction;
                for (String dependency : dependencies) {
                    heraAction = this.findById(Long.parseLong(dependency));
                    if (heraAction != null) {
                        if (StatusEnum.SUCCESS.toString().equals(heraAction.getStatus())) {
                            builder.append(Constants.HTML_FONT_GREEN_LEFT).append("依赖任务:").append(dependency).append(",结束时间:").append(ActionUtil.getFormatterDate(ActionUtil.MON_MIN, heraAction.getStatisticEndTime()));
                        } else if (StatusEnum.RUNNING.toString().equals(heraAction.getStatus())) {
                            builder.append(Constants.HTML_FONT_BLUE_LEFT).append("依赖任务:").append(dependency).append(",执行中");
                        } else if (StatusEnum.FAILED.toString().equals(heraAction.getStatus()) || StatusEnum.WAIT.toString().equals(heraAction.getStatus())) {
                            builder.append(Constants.HTML_FONT_RED_LEFT).append("依赖任务:").append(dependency).append(",执行失败");
                        } else {
                            builder.append(Constants.HTML_FONT_RED_LEFT).append("依赖任务:").append(dependency).append(",未执行");
                        }
                    } else {
                        builder.append(Constants.HTML_FONT_RED_LEFT).append("依赖任务:").append(dependency).append(",未找到");
                    }
                    builder.append(Constants.HTML_FONT_RIGHT).append(Constants.HTML_NEW_LINE);
                }
                taskVo.setReadyStatus(builder.toString());
            }
            res.add(taskVo);
        });
        return res;
    }

    @Override
    public void deleteHistoryRecord(Integer beforeDay) {
        heraJobActionMapper.deleteHistoryRecord(beforeDay);
    }

    @Override
    public void deleteAllHistoryRecord(Integer beforeDay) {
        this.deleteHistoryRecord(beforeDay);
        heraJobHistoryService.deleteHistoryRecord(beforeDay);
    }

    @Override
    public List<HeraAction> findByStartAndEnd(Long startAction, Long endAction, Integer jobId, Integer limit) {
        return heraJobActionMapper.selectByStartAndEnd(startAction, endAction, jobId, limit);
    }

    @Override
    public boolean deleteAction(long startAction, long endAction, Integer jobId) {
        Integer integer = heraJobActionMapper.deleteAction(startAction, endAction, jobId);
        return integer != null && integer > 0;
    }

    @Override
    public HeraAction findTodaySuccessByJobId(int jobId) {
        return heraJobActionMapper.selectTodaySuccessByJobId(jobId);
    }

    @Override
    public List<HeraActionMani> getAllManifest(Long endAction) {
        return heraJobActionMapper.getAllManifest(Long.parseLong(ActionUtil.getTodayAction()), endAction);
    }

    private String buildFont(String str, String type) {
        if (type == null) {
            return Constants.HTML_FONT_RED_LEFT + str + Constants.HTML_FONT_RIGHT;
        }

        if (StatusEnum.RUNNING.toString().equals(type)) {
            return Constants.HTML_FONT_BLUE_LEFT + str + Constants.HTML_FONT_RIGHT;
        }
        if (StatusEnum.SUCCESS.toString().equals(type)) {
            return Constants.HTML_FONT_GREEN_LEFT + str + Constants.HTML_FONT_RIGHT;
        }
        if (StatusEnum.FAILED.toString().equals(type)) {
            return Constants.HTML_FONT_RED_LEFT + str + Constants.HTML_FONT_RIGHT;
        }
        if (Constants.STATUS_NONE.equals(type)) {
            return Constants.HTML_FONT_LEFT + str + Constants.HTML_FONT_RIGHT;
        }
        return Constants.HTML_FONT_RED_LEFT + str + Constants.HTML_FONT_RIGHT;

    }
}
