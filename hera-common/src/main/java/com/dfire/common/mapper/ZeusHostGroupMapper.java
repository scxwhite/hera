package com.dfire.common.mapper;

import com.dfire.common.entity.ZeusHostGroup;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 20:48 2018/1/10
 * @desc
 */
public interface ZeusHostGroupMapper {

    @Select("SELECT HOST FROM ZEUS_HOST_RELATION WHERE HOST_GROUP_ID = #{preemptionMasterGroupId}")
    List<String> getPreemptionGroup(@Param("preemptionMasterGroupId") String groupId);


    @Select("SELECT * FROM ZEUS_HOST_GROUP ")
    public List<ZeusHostGroup> getAllHostGroupList();



}
