package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraUser;
import com.dfire.common.mapper.HeraUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dfire.common.service.HeraUserService;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 0:35 2017/12/30
 * @desc
 */
@Service("heraUserService")
public class HeraUserServiceImpl implements HeraUserService {

    @Autowired
    private HeraUserMapper heraUserMapper;

    @Override
    public Boolean addUser(HeraUser user) {

        Integer res = heraUserMapper.insert(user);

        return res != null && res > 0;
    }

    @Override
    public HeraUser findByName(String name) {
        return heraUserMapper.findByName(name);
    }

    @Override
    public List<HeraUser> getAllUsers() {
        return null;
    }

    @Override
    public HeraUser findByUid(String uid) {
        return null;
    }

    @Override
    public List<HeraUser> findListByUid(List<String> uids) {
        return null;
    }

    @Override
    public HeraUser addOrUpdateUser(HeraUser user) {
        return null;
    }

    @Override
    public List<HeraUser> findListByUidByOrder(List<String> uids) {
        return null;
    }

    @Override
    public HeraUser findByUidFilter(String uid) {
        return null;
    }

    @Override
    public List<HeraUser> findAllUsers(String sortField, String sortOrder) {
        return null;
    }

    @Override
    public List<HeraUser> findListByFilter(String filter, String sortField, String sortOrder) {
        return null;
    }

    @Override
    public void update(HeraUser user) {

    }
}
