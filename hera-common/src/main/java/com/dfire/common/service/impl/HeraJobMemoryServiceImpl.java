package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.Judge;
import com.dfire.common.entity.vo.HeraJobTreeNodeVo;
import com.dfire.common.mapper.HeraJobMapper;
import com.dfire.common.service.HeraJobService;
import com.dfire.common.vo.RestfulResponse;
import com.dfire.graph.JobRelation;
import com.dfire.logs.HeraLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author xiaosuda
 * @date 2018/12/3
 */
@Service("heraJobMemoryService")
public class HeraJobMemoryServiceImpl implements HeraJobService {

    private Judge judge;

    @Autowired
    private HeraJobMapper heraJobMapper;

    private Map<Integer, HeraJob> memoryJob;

    private Map<Integer, HeraJob> getMemoryJob() {
        Judge newJudge = heraJobMapper.selectTableInfo();
        if (judge == null || !judge.getCount().equals(newJudge.getCount()) || !judge.getLastModified().equals(newJudge.getLastModified()) || !judge.getMaxId().equals(newJudge.getMaxId())) {
            HeraLog.info("刷新hera_job库");
            judge = new Judge();
            List<HeraJob> all = heraJobMapper.getAll();
            Map<Integer, HeraJob> jobMap = new HashMap<>(all.size());
            all.forEach(job -> jobMap.put(job.getId(), job));
            memoryJob = jobMap;
        }
        judge.setStamp(new Date());
        return memoryJob;
    }

    @Override
    public int insert(HeraJob heraJob) {
        return heraJobMapper.insert(heraJob);
    }

    @Override
    public int delete(int id) {
        return heraJobMapper.delete(id);
    }

    @Override
    public Integer update(HeraJob heraJob) {
        return heraJobMapper.update(heraJob);
    }

    @Override
    public List<HeraJob> getAll() {
        return (List<HeraJob>) getMemoryJob().values();
    }

    @Override
    public HeraJob findById(int id) {
        return memoryJob.get(id);
    }

    @Override
    public List<HeraJob> findByIds(List<Integer> list) {
        List<HeraJob> res = new ArrayList<>();
        Map<Integer, HeraJob> memoryJob = getMemoryJob();
        list.forEach(id -> res.add(memoryJob.get(id)));
        return res;
    }

    @Override
    public List<HeraJob> findByPid(int groupId) {
        return heraJobMapper.findByPid(groupId);
    }

    @Override
    public Map<String, List<HeraJobTreeNodeVo>> buildJobTree(String owner) {
        return null;
    }

    @Override
    public boolean changeSwitch(Integer id) {
        return false;
    }

    @Override
    public RestfulResponse checkAndUpdate(HeraJob heraJob) {
        return null;
    }

    @Override
    public Map<String, Object> findCurrentJobGraph(int jobId, Integer type) {
        return null;
    }

    @Override
    public List<JobRelation> getJobRelations() {
        return null;
    }

    @Override
    public List<HeraJob> findAllDependencies() {
        return null;
    }

    @Override
    public List<HeraJob> findDownStreamJob(Integer jobId) {
        return null;
    }

    @Override
    public List<HeraJob> findUpStreamJob(Integer jobId) {
        return null;
    }
}
