package com.dfire.common.service;

import com.dfire.common.entity.ZeusLock;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 14:28 2018/1/12
 * @desc
 */
public interface ZeusLockService {

    public ZeusLock getZeusLock(String subGroup) ;

    public void save(ZeusLock zeusLock);
}
