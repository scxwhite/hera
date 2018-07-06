package com.dfire.common.service;

import com.dfire.common.entity.HeraHostGroup;
import com.dfire.common.entity.vo.HeraHostGroupVo;

import java.util.List;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 20:49 2018/1/10
 * @desc
 */
public interface HeraHostGroupService {


    int insert(HeraHostGroup heraHostGroup);

    int delete(int id);

    int update(HeraHostGroup heraHostGroup);

    List<HeraHostGroup> getAll();

    HeraHostGroup findById(int id);

    /**
     * 查询出所有的host组 如：本地 测试 etc.
     * @return
     */
    Map<Integer, HeraHostGroupVo> getAllHostGroupInfo();


}
