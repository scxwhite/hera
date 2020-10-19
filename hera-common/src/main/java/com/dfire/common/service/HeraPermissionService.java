package com.dfire.common.service;

import com.dfire.common.entity.HeraPermission;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午3:41 2018/5/16
 * @desc
 */
public interface HeraPermissionService {

    int insert(HeraPermission heraPermission);

    List<HeraPermission> findByTargetId(Integer targetId, String type, Integer isValid);

    HeraPermission findByCond(Integer id, String owner, String type);

    Integer updateByUid(Integer id, String type, Integer isValid, String uId);


    Integer insertList(List<HeraPermission> permissions);
}
