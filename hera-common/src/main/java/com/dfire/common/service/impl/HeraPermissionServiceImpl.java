package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraAction;
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
}
