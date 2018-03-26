package com.dfire.common.service.impl;

import com.dfire.common.entity.ZeusUser;
import com.dfire.common.mapper.ZeusUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dfire.common.service.ZeusUserService;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 0:35 2017/12/30
 * @desc
 */
@Service("zeusUserServiceImpl")
public class ZeusUserServiceImpl  implements ZeusUserService {

    @Autowired
    private ZeusUserMapper zeusUserMapper;

    public int addUser(String name, Integer age) {
        return zeusUserMapper.insert(name, age);
    }

    public ZeusUser findByName(String name) {
        return zeusUserMapper.findByName(name);
    }
}
