package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraJob;
import com.dfire.common.mapper.HeraJobMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dfire.common.service.HeraJobService;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 2:08 2018/1/11
 * @desc
 */
@Service("heraJobServiceImpl")
public class HeraJobServiceImpl implements HeraJobService {

    @Autowired
    private HeraJobMapper heraJobMapper;

    public HeraJob findByName(int id) {
        return heraJobMapper.findByName(id);
    }

    @Override
    public HeraJob findById(String jobId) {
        return heraJobMapper.findById(jobId);
    }
}
