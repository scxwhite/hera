package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraGroup;
import com.dfire.common.mapper.HeraGroupMapper;
import com.dfire.common.service.HeraGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午5:02 2018/4/17
 * @desc
 */
@Service("heraGroupService")
public class HeraGroupServiceImpl implements HeraGroupService {

    @Autowired
    private HeraGroupMapper heraGroupMapper;

    @Override
    public HeraGroup getGlobalGroup() {
        return heraGroupMapper.getGlobalGroup();
    }
}
