package com.dfire.common.service.impl;

import com.dfire.common.entity.ZeusJob;
import com.dfire.common.mapper.ZeusJobMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dfire.common.service.ZeusJobService;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 2:08 2018/1/11
 * @desc
 */
@Service("zeusJobServiceImpl")
public class ZeusJobServiceImpl implements ZeusJobService {

    @Autowired
    private ZeusJobMapper zeusJobMapper;

    public ZeusJob findByName(int id) {
        return zeusJobMapper.findByName(id);
    }
}
