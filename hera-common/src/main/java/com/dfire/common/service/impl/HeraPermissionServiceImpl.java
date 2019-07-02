package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraPermission;
import com.dfire.common.mapper.HeraPermissionMapper;
import com.dfire.common.service.HeraPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午3:42 2018/5/16
 * @desc
 */
@Service("heraPermissionService")
public class HeraPermissionServiceImpl implements HeraPermissionService {

    @Autowired
    private HeraPermissionMapper permissionMapper;

    @Override
    public int insert(HeraPermission heraPermission) {
        heraPermission.setIsValid(1);
        permissionMapper.insert(heraPermission);
        return heraPermission.getId();
    }


    @Override
    public List<HeraPermission> findByTargetId(Integer targetId, String type, Integer isValid) {
        return permissionMapper.findByTargetId(targetId, type, isValid);
    }

    @Override
    public HeraPermission findByCond(Integer id, String owner, String type) {
        return permissionMapper.findByCond(id, owner, type);
    }

    @Override
    public Integer updateByUid(Integer id, String type, Integer isValid, String uId) {
        return permissionMapper.updateByUid(id, type, isValid, uId);
    }

    @Override
    public Integer insertList(List<HeraPermission> permissions) {
        return permissionMapper.insertList(permissions);
    }

}
