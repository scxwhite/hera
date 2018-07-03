package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraGroup;
import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.vo.HeraJobTreeNodeVo;
import com.dfire.common.mapper.HeraJobMapper;
import com.dfire.common.service.HeraGroupService;
import com.dfire.common.service.HeraJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 2:08 2018/1/11
 * @desc
 */
@Service("heraJobService")
public class HeraJobServiceImpl implements HeraJobService {

    @Autowired
    private HeraJobMapper heraJobMapper;
    @Autowired
    private HeraGroupService groupService;

    @Override
    public int insert(HeraJob heraJob) {
        Date date = new Date();
        heraJob.setGmtCreate(date);
        heraJob.setGmtModified(date);
        heraJob.setAuto(0);
        return heraJobMapper.insert(heraJob);
    }

    @Override
    public int delete(int id) {
        return heraJobMapper.delete(id);
    }

    @Override
    public int update(HeraJob heraJob) {
        return heraJobMapper.update(heraJob);
    }

    @Override
    public List<HeraJob> getAll() {
        return heraJobMapper.getAll();
    }

    @Override
    public HeraJob findById(int id) {
        HeraJob heraJob = HeraJob.builder().id(id).build();
        return heraJobMapper.findById(heraJob);
    }

    @Override
    public List<HeraJob> findByIds(List<Integer> list) {
        return heraJobMapper.findByIds(list);
    }

    @Override
    public List<HeraJob> findByPid(int groupId) {
        HeraJob heraJob = HeraJob.builder().groupId(groupId).build();
        return heraJobMapper.findByPid(heraJob);
    }

    @Override
    public List<HeraJobTreeNodeVo> buildJobTree() {
        List<HeraGroup> groups = groupService.getAll();
        List<HeraJob> jobs = heraJobMapper.getAll();
        List<HeraJobTreeNodeVo> list = groups.stream()
                .filter(group -> group.getExisted() == 1)
                .map(g -> HeraJobTreeNodeVo.builder().id(g.getId() + "").parent(g.getParent() + "").directory(g.getDirectory()).isParent(true).name(g.getName() + "(" + g.getId() + ")").build()
                ).collect(Collectors.toList());
        List<HeraJobTreeNodeVo> jobList = jobs.stream()
                .map(job -> HeraJobTreeNodeVo.builder().id(job.getId() + "").parent(job.getGroupId() + "").isParent(false).name(job.getName() + "(" + job.getId() + ")").build()).collect(Collectors.toList());
        list.addAll(jobList);
        return list;
    }

    @Override
    public boolean changeSwitch(Integer id) {
        Integer res = heraJobMapper.updateSwitch(id);
        return res != null && res > 0;
    }
}
