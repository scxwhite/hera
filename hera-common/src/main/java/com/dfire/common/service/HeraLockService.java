package com.dfire.common.service;

import com.dfire.common.entity.HeraLock;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 14:28 2018/1/12
 * @desc
 */
public interface HeraLockService {

    HeraLock findById(String group);

    int insert(HeraLock heraLock);

    int update(HeraLock heraLock);
}
