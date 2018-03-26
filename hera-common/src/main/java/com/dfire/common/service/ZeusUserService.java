package com.dfire.common.service;

import com.dfire.common.entity.ZeusUser;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 0:35 2017/12/30
 * @desc
 */

public interface ZeusUserService {

    public int addUser(String name, Integer age) ;

    public ZeusUser findByName(String name);

}
