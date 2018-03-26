package com.dfire.common.service;

import com.dfire.common.entity.ZeusHostGroup;
import com.dfire.common.vo.ZeusHostGroupVo;

import java.util.List;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 20:49 2018/1/10
 * @desc
 */
public interface ZeusHostGroupService {


    public List<String> getPreemptionGroup(String preemptionMasterGroup);


    public List<ZeusHostGroup> getAllHostGroupList();

    Map<String, ZeusHostGroupVo> getAllHostGroupInfo();

}
