package com.dfire.common.service;

import com.dfire.common.entity.HeraUser;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 0:35 2017/12/30
 * @desc
 */

public interface HeraUserService {

    int insert(HeraUser heraUser);

    int delete(Integer id);

    boolean update(HeraUser heraUser);

    List<HeraUser> getAll();

    HeraUser findById(Integer id);

    HeraUser findByName(String name);

    List<HeraUser> findByIds(List<Integer> list);

    int updateEffective(Integer id, String effective);

    List<HeraUser> findAllName();

    List<HeraUser> getGroups();

}
