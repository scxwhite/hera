package com.dfire.common.service;

import com.dfire.common.entity.HeraUser;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 0:35 2017/12/30
 * @desc
 */

public interface HeraUserService {

    public int addUser(String name, Integer age) ;

    public HeraUser findByName(String name);

}
