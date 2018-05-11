package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraLock;
import com.dfire.common.mapper.HeraLockMapper;
import com.dfire.common.service.HeraLockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 14:29 2018/1/12
 * @desc hera_lock基于数据实现的分布式锁
 */
@Service("heraLockService")
public class HeraLockServiceImpl implements HeraLockService {

    @Autowired
    private HeraLockMapper heraLockMapper;

    /**
     * @desc 分布式锁保持唯一性，设为单例模式
     * @param subGroup
     * @return
     */
    @Bean(name="lock")
    @Scope("prototype")
    public HeraLock getHeraLock(String subGroup) {
        return heraLockMapper.getHeraLock(subGroup);
    }

    public void update(HeraLock heraLock) {
        heraLockMapper.update(heraLock);
    }
}
