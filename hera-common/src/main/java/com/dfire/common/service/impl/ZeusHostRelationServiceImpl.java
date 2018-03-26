package com.dfire.common.service.impl;

import com.dfire.common.entity.ZeusHostRelation;
import com.dfire.common.mapper.ZeusHostRelationMapper;
import com.dfire.common.service.ZeusHostRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 17:08 2018/1/12
 * @desc
 */
@Service("zeusHostRelationService")
public class ZeusHostRelationServiceImpl implements ZeusHostRelationService {

    @Autowired
    private ZeusHostRelationMapper zeusHostRelationMapper;

    @Override
    public List<ZeusHostRelation> getAllHostRelationList() {
        return zeusHostRelationMapper.getAllHostRelationList();
    }
}
