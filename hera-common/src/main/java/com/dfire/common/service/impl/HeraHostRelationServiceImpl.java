package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraHostRelation;
import com.dfire.common.mapper.HeraHostRelationMapper;
import com.dfire.common.service.HeraHostRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 17:08 2018/1/12
 * @desc
 */
@Service("heraHostRelationService")
public class HeraHostRelationServiceImpl implements HeraHostRelationService {

    @Autowired
    private HeraHostRelationMapper heraHostRelationMapper;

    @Override
    public int insert(HeraHostRelation heraHostRelation) {
        return heraHostRelationMapper.insert(heraHostRelation);
    }

    @Override
    public int delete(int id) {
        return heraHostRelationMapper.delete(id);
    }

    @Override
    public int update(HeraHostRelation heraHostRelation) {
        return heraHostRelationMapper.update(heraHostRelation);
    }

    @Override
    public List<HeraHostRelation> getAll() {
        return heraHostRelationMapper.getAll();
    }

    @Override
    public HeraHostRelation findById(HeraHostRelation heraHostRelation) {
        return heraHostRelationMapper.findById(heraHostRelation);
    }

    @Override
    public List<String> findPreemptionGroup(int id) {
        return heraHostRelationMapper.findPreemptionGroup(id);
    }
}
