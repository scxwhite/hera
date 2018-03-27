package com.dfire.common.mapper;

import com.dfire.common.entity.HeraHostGroup;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 20:48 2018/1/10
 * @desc
 */
public interface HeraHostGroupMapper {

    @Select("SELECT HOST FROM HERA_HOST_RELATION WHERE HOST_GROUP_ID = #{preemptionMasterGroupId}")
    List<String> getPreemptionGroup(@Param("preemptionMasterGroupId") String groupId);


    @Select("SELECT * FROM HERA_HOST_GROUP ")
    public List<HeraHostGroup> getAllHostGroupList();



}
