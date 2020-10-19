package com.dfire.common.service;

import com.dfire.common.entity.HeraProfile;
import com.dfire.common.entity.vo.HeraProfileVo;
import org.springframework.stereotype.Service;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 19:22 2018/1/12
 * @desc
 */
public interface HeraProfileService {

    HeraProfileVo findByOwner(String owner);

    void insert(HeraProfile profile);

    void update(HeraProfile profile);
}
