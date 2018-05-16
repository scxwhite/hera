package com.dfire.common.mapper;

import com.dfire.common.entity.HeraProfile;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午3:46 2018/5/1
 * @desc
 */
public interface HeraProfileMapper {

    void update(String uid, HeraProfile p);

    public HeraProfile findByUid(String uid);

}
