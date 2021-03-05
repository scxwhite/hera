package com.dfire.common.service;

import com.dfire.common.entity.HeraAction;
import com.dfire.common.entity.model.TablePageForm;
import com.dfire.common.entity.vo.HeraActionMani;
import com.dfire.common.entity.vo.HeraActionVo;
import com.dfire.common.kv.Tuple;
import com.dfire.common.vo.GroupTaskVo;
import com.dfire.common.vo.JobStatus;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午3:41 2018/5/16
 * @desc
 */
public interface HeraJobActionService {


    int insert(HeraAction heraAction, Long nowAction);

    /**
     * 批量插入
     *
     * @param heraActionList
     * @return
     */
    List<HeraAction> batchInsert(List<HeraAction> heraActionList, Long nowAction);

    int delete(Long id);

    int update(HeraAction heraAction);

    List<HeraAction> getAll();

    HeraAction findById(Long actionId);

    HeraAction findById(String actionId);

    HeraAction findLatestByJobId(Long jobId);

    List<HeraAction> findByJobId(Long jobId);

    int updateStatus(JobStatus jobStatus);

    Tuple<HeraActionVo, JobStatus> findHeraActionVo(Long jobId);

    /**
     * 查找当前版本的运行状态
     *
     * @param actionId
     * @return
     */
    JobStatus findJobStatus(Long actionId);


    JobStatus findJobStatusByJobId(Long jobId);


    Integer updateStatus(Long id, String status);

    Integer updateStatusAndReadDependency(HeraAction heraAction);


    List<HeraAction> getAfterAction(Long action);

    /**
     * 根据jobId 获取所有的版本
     *
     * @param jobId
     * @return
     */
    List<Long> getActionVersionByJobId(Long jobId);

    List<HeraActionVo> getNotRunScheduleJob();

    List<HeraActionVo> getFailedJob();

    List<GroupTaskVo> findByJobIds(List<Integer> idList, String startDate, String endDate, TablePageForm pageForm, String status);

    void deleteHistoryRecord(Integer beforeDay);

    void deleteAllHistoryRecord(Integer beforeDay);

    List<HeraAction> findByStartAndEnd(Long startAction, Long endAction, Integer jobId,Integer limit);

    boolean deleteAction(long startAction, long endAction, Integer jobId);

    HeraAction findTodaySuccessByJobId(int id);

    List<HeraActionMani> getAllManifest(Long endAction);
}
