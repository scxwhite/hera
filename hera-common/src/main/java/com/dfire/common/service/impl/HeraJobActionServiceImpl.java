package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraAction;
import com.dfire.common.mapper.HeraJobActionMapper;
import com.dfire.common.service.HeraJobActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午3:43 2018/5/16
 * @desc
 */
@Service("heraJobActionService")
public class HeraJobActionServiceImpl implements HeraJobActionService {

    @Autowired
    private HeraJobActionMapper heraJobActionMapper;

    @Override
    public int insert(HeraAction heraAction) {
        return heraJobActionMapper.insert(heraAction);
    }

    @Override
    public int delete(String id) {
        return heraJobActionMapper.delete(id);
    }

    @Override
    public int update(HeraAction heraAction) {
        return heraJobActionMapper.update(heraAction);
    }

    @Override
    public List<HeraAction> getAll() {
        return heraJobActionMapper.getAll();
    }

    @Override
    public HeraAction findById(HeraAction heraAction) {
        return heraJobActionMapper.findById(heraAction);
    }

    @Override
    public HeraAction findByJobId(HeraAction heraAction) {
        return heraJobActionMapper.findByJobId(heraAction);
    }
}
