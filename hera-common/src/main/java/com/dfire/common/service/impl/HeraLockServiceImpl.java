package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraLock;
import com.dfire.common.mapper.HeraLockMapper;
import com.dfire.common.service.HeraLockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 14:29 2018/1/12
 * @desc hera_lock基于数据实现的分布式锁
 */
@Service("heraLockService")
public class HeraLockServiceImpl implements HeraLockService {

    @Autowired
    private HeraLockMapper heraLockMapper;


    @Bean(name = "lock")
    @Scope("prototype")
    @Override
    public HeraLock findBySubgroup(String group) {
        return heraLockMapper.findBySubgroup(group);
    }

    @Override
    public Integer insert(HeraLock heraLock) {
        return heraLockMapper.insert(heraLock);
    }

    @Override
    public int update(HeraLock heraLock) {
        return heraLockMapper.update(heraLock);
    }

    @Override
    public Integer changeLock(String host, Date serverUpdate, Date gmtModified, String lastHost) {
        return heraLockMapper.updateLock(host, serverUpdate, gmtModified, lastHost);
    }

}
