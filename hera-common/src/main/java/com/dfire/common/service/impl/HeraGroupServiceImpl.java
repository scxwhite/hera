package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraGroup;
import com.dfire.common.entity.model.HeraJobBean;
import com.dfire.common.mapper.HeraGroupMapper;
import com.dfire.common.mapper.HeraJobMapper;
import com.dfire.common.service.HeraGroupService;
import graph.JobGroupGraphTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午5:02 2018/4/17
 * @desc
 */
@Service("heraGroupService")
public class HeraGroupServiceImpl implements HeraGroupService {

    @Autowired
    private HeraGroupMapper heraGroupMapper;
    @Autowired
    private HeraJobMapper heraJobMapper;

    @Override
    public HeraGroup getRootGroup() {
        HeraGroup rootGroup = findById(3578);
        return rootGroup;
    }


    @Override
    public HeraJobBean getUpstreamJobBean(String actionId) {
        //todo 完成层级属性封装
        return JobGroupGraphTool.getUpstreamJobBean(actionId);
    }


    @Override
    public int insert(HeraGroup heraFile) {
        return heraGroupMapper.insert(heraFile);
    }

    @Override
    public int delete(int id) {
        return heraGroupMapper.delete(id);
    }

    @Override
    public int update(HeraGroup heraFile) {
        return heraGroupMapper.update(heraFile);
    }

    @Override
    public List<HeraGroup> getAll() {
        return heraGroupMapper.getAll();
    }

    @Override
    public HeraGroup findById(int id) {
        HeraGroup heraGroup = HeraGroup.builder().id(id).build();
        return heraGroupMapper.findById(heraGroup);
    }

    @Override
    public List<HeraGroup> findByIds(List<Integer> list) {
        return heraGroupMapper.findByIds(list);
    }

    @Override
    public List<HeraGroup> findByParent(int parentId) {
        HeraGroup heraGroup = HeraGroup.builder().parent(parentId).build();
        return heraGroupMapper.findByParent(heraGroup);
    }

    @Override
    public List<HeraGroup> findByOwner(String owner) {
        HeraGroup heraGroup = HeraGroup.builder().owner(owner).build();
        return heraGroupMapper.findByOwner(heraGroup);
    }
}
