package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraProfile;
import com.dfire.common.entity.vo.HeraProfileVo;
import com.dfire.common.mapper.HeraProfileMapper;
import com.dfire.common.service.HeraProfileService;
import com.dfire.common.util.BeanConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 19:23 2018/1/12
 * @desc
 */
@Service("heraProfileService")
public class HeraProfileServiceImpl implements HeraProfileService {

    @Autowired
    private HeraProfileMapper heraProfileMapper;


    @Override
    public HeraProfileVo findByOwner(String owner) {
        return null;
    }

    @Override
    public void insert(HeraProfile profile) {

    }

    @Override
    public void update(HeraProfile profile) {

    }
}
