package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraDebugHistory;
import com.dfire.common.mapper.HeraDebugHistoryMapper;
import com.dfire.common.service.HeraDebugHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午7:27 2018/4/16
 * @desc
 */
@Service("heraDebugHistoryService")
public class HeraDebugHistoryServiceImpl implements HeraDebugHistoryService {

    @Autowired
    HeraDebugHistoryMapper heraDebugHistoryMapper;

    @Override
    public void addHeraDebugHistory(HeraDebugHistory heraDebugHistory) {
        heraDebugHistoryMapper.addHeraDebugHistory(heraDebugHistory);
    }
}
