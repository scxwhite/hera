package com.dfire.common.service;

import com.dfire.common.entity.HeraUser;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 0:35 2017/12/30
 * @desc
 */

public interface HeraUserService {

    public int addUser(String name, Integer age) ;

    public HeraUser findByName(String name);

    public List<HeraUser> getAllUsers();

    public HeraUser findByUid(String uid);

    public List<HeraUser> findListByUid(List<String> uids);

    public HeraUser addOrUpdateUser(HeraUser user);

    public List<HeraUser> findListByUidByOrder(List<String> uids);

    public HeraUser findByUidFilter(String uid);

    public List<HeraUser> findAllUsers(String sortField, String sortOrder);

    public List<HeraUser> findListByFilter(String filter, String sortField, String sortOrder);

    public void update(HeraUser user);

}
