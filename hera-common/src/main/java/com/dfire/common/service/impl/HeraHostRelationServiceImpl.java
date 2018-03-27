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
    public List<HeraHostRelation> getAllHostRelationList() {
        return heraHostRelationMapper.getAllHostRelationList();
    }
}
