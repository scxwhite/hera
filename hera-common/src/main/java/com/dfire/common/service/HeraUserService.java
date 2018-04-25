package com.dfire.common.service;

import com.dfire.common.entity.HeraUser;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 0:35 2017/12/30
 * @desc
 */

public interface HeraUserService {

    Boolean addUser(HeraUser user) ;

    HeraUser findByName(String name);

    List<HeraUser> getAllUsers();

    HeraUser findByUid(String uid);

    List<HeraUser> findListByUid(List<String> uids);

    HeraUser addOrUpdateUser(HeraUser user);

    List<HeraUser> findListByUidByOrder(List<String> uids);

    HeraUser findByUidFilter(String uid);

    List<HeraUser> findAllUsers(String sortField, String sortOrder);

    List<HeraUser> findListByFilter(String filter, String sortField, String sortOrder);

    void update(HeraUser user);

}
