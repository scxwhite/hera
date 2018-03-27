package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraUser;
import com.dfire.common.mapper.HeraUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dfire.common.service.HeraUserService;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 0:35 2017/12/30
 * @desc
 */
@Service("heraUserServiceImpl")
public class HeraUserServiceImpl implements HeraUserService {

    @Autowired
    private HeraUserMapper heraUserMapper;

    public int addUser(String name, Integer age) {
        return heraUserMapper.insert(name, age);
    }

    public HeraUser findByName(String name) {
        return heraUserMapper.findByName(name);
    }
}
