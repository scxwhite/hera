package com.dfire.common.service;

import com.dfire.common.entity.HeraHostGroup;
import com.dfire.common.entity.vo.HostGroupVo;
import com.dfire.common.vo.HeraHostGroupVo;
import com.dfire.common.vo.RestfulResponse;

import java.util.List;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 20:49 2018/1/10
 * @desc
 */
public interface HeraHostGroupService {


    List<String> getPreemptionGroup(String preemptionMasterGroup);


    /**
     * 查询所有机器组
     * @return List
     */
    List<HeraHostGroup> getAllHostGroupList();

    Map<String, HeraHostGroupVo> getAllHostGroupInfo();



    /**
     * 根据Id删除该机器组
     * @param id    机器组id
     * @return  Boolean
     */
    Boolean deleteById(Integer id);

    /**
     * 添加机器组
     * @param hostGroup hostGroup
     * @return Boolean
     */
    Boolean addHostGroup(HeraHostGroup hostGroup);

    /**
     * 根据hostGroup.id更新hostGroup
     * @param hostGroup hostGroup
     * @return Boolean
     */
    Boolean updateHostGroup(HeraHostGroup hostGroup);

    /**
     * 根据id判断是更新还是插入操作
     * @param heraHostGroup heraHostGroup
     * @return RestfulResponse RestfulResponse
     */
    RestfulResponse saveOrUpdate(HeraHostGroup heraHostGroup);


}
