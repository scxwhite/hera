package com.dfire.common.service;

import com.dfire.common.entity.HeraLock;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 14:28 2018/1/12
 * @desc
 */
public interface HeraLockService {

    public HeraLock getHeraLock(String subGroup) ;

    public void updateHeraLock(HeraLock heraLock);

    public void insertHeraLock(HeraLock heraLock);
}
