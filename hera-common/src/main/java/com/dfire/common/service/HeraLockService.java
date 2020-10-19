package com.dfire.common.service;

import com.dfire.common.entity.HeraLock;

import java.util.Date;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 14:28 2018/1/12
 * @desc
 */
public interface HeraLockService {

    HeraLock findBySubgroup(String group);

    Integer insert(HeraLock heraLock);

    int update(HeraLock heraLock);

    Integer changeLock(String host, Date serverUpdate, Date gmtModified, String lastHost);
}
