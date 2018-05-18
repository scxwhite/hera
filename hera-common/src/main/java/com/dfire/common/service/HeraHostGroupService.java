package com.dfire.common.service;

import com.dfire.common.entity.HeraHostGroup;
import com.dfire.common.entity.vo.HostGroupVo;
import com.dfire.common.mybatis.HeraInsertLangDriver;
import com.dfire.common.mybatis.HeraSelectLangDriver;
import com.dfire.common.mybatis.HeraUpdateLangDriver;
import com.dfire.common.vo.HeraHostGroupVo;
import com.dfire.common.vo.RestfulResponse;
import org.apache.ibatis.annotations.*;

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

    public Map<String, HeraHostGroupVo> getAllHostGroupInfo();


}
