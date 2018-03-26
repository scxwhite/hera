package com.dfire.common.service.impl;

import com.dfire.common.entity.ZeusLock;
import com.dfire.common.mapper.ZeusLockMapper;
import com.dfire.common.service.ZeusLockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 14:29 2018/1/12
 * @desc zeus_lock基于数据实现的分布式锁
 */
@Service("zeusLockService")
public class ZeusLockServiceImpl implements ZeusLockService {

    @Autowired
    private ZeusLockMapper zeusLockMapper;

    /**
     * @desc 分布式锁保持唯一性，设为单例模式
     * @param subGroup
     * @return
     */
    @Bean(name="lock")
    @Scope("prototype")
    public ZeusLock getZeusLock(String subGroup) {
        return zeusLockMapper.getZeusLock(subGroup);
    }

    public void save(ZeusLock zeusLock) {
        zeusLockMapper.save(zeusLock);
    }
}
