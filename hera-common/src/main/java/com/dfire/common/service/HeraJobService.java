package com.dfire.common.service;

import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.vo.HeraJobTreeNodeVo;
import com.dfire.common.enums.RunAuthType;

import java.util.List;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 2:08 2018/1/11
 * @desc
 */
public interface HeraJobService {

    int insert(HeraJob heraJob);

    int delete(int id);

    Integer update(HeraJob heraJob);

    List<HeraJob> getAll();

    HeraJob findById(int id);

    Integer findMustEndMinute(int id);

    List<HeraJob> findEstimatedEndHours(int startTime, int endTime);

    HeraJob findMemById(int id);

    List<HeraJob> findByIds(List<Integer> list);

    List<HeraJob> findByPid(int groupId);

    /**
     * 构建job树形目录结构
     *
     * @return
     */
    Map<String, List<HeraJobTreeNodeVo>> buildJobTree(String owner,Integer ssoId);

    boolean changeSwitch(Integer id, Integer status);


    Map<String, Object> findCurrentJobGraph(int jobId, Integer type);


    List<Integer> findJobImpact(int jobId, Integer type);


    List<HeraJob> findDownStreamJob(Integer jobId);

    List<HeraJob> findUpStreamJob(Integer jobId);

    List<HeraJob> getAllJobDependencies();


    boolean changeParent(Integer newId, Integer parentId);

    boolean isRepeat(Integer jobId);

    Integer updateScript(Integer id, String script);

    Integer selectMaxId();

    String checkDependencies(Integer xId, RunAuthType type);
}
