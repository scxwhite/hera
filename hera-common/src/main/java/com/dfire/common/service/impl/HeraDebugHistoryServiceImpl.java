package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraDebugHistory;
import com.dfire.common.entity.vo.HeraDebugHistoryVo;
import com.dfire.common.mapper.HeraDebugHistoryMapper;
import com.dfire.common.mybatis.HeraInsertLangDriver;
import com.dfire.common.mybatis.HeraSelectLangDriver;
import com.dfire.common.mybatis.HeraUpdateLangDriver;
import com.dfire.common.service.HeraDebugHistoryService;
import com.dfire.common.util.BeanConvertUtils;
import org.apache.ibatis.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public String insert(HeraDebugHistory heraDebugHistory) {
        heraDebugHistoryMapper.insert(heraDebugHistory);
        return heraDebugHistory.getId();
    }

    @Override
    public int delete(int id) {
        return heraDebugHistoryMapper.delete(id);
    }

    @Override
    public int update(HeraDebugHistory heraDebugHistory) {
        return heraDebugHistoryMapper.update(heraDebugHistory);
    }

    @Override
    public List<HeraDebugHistory> getAll() {
        return heraDebugHistoryMapper.getAll();
    }

    @Override
    public HeraDebugHistoryVo findById(String id) {
        HeraDebugHistory debugHistory = HeraDebugHistory.builder().id(id).build();
        HeraDebugHistory history= heraDebugHistoryMapper.findById(debugHistory);
        return BeanConvertUtils.convert(history);
    }


}
