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
        permissionMapper.insert(heraPermission);
        return heraPermission.getId();
    }

    @Override
    public int delete(String id) {
        return permissionMapper.delete(id);
    }

    @Override
    public int update(HeraPermission heraPermission) {
        return permissionMapper.update(heraPermission);
    }

    @Override
    public List<HeraPermission> getAll() {
        return permissionMapper.getAll();
    }

    @Override
    public HeraPermission findById(HeraPermission heraPermission) {
        return permissionMapper.findById(heraPermission);
    }

    @Override
    public List<HeraPermission> findByIds(List<Integer> list) {
        return permissionMapper.findByIds(list);
    }

    @Override
    public List<HeraPermission> findByTargetId(Integer targetId) {
        return permissionMapper.findByTargetId(targetId);
    }

    @Override
    public HeraPermission findByCond(Integer id, String owner) {
        return permissionMapper.findByCond(id, owner);
    }

    @Override
    public Integer deleteByTargetId(Integer id) {
        return permissionMapper.deleteByTargetId(id);
    }

    @Override
    public Integer insertList(List<HeraPermission> permissions) {
        return permissionMapper.insertList(permissions);
    }

}
