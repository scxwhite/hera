package com.dfire.common.mapper;

import com.dfire.common.entity.HeraHostGroup;
import org.apache.ibatis.annotations.*;
import org.omg.CORBA.INTERNAL;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 20:48 2018/1/10
 * @desc
 */
public interface HeraHostGroupMapper {

    @Select("SELECT HOST FROM hera_host_relation WHERE host_group_id = #{preemptionMasterGroupId}")
    List<String> getPreemptionGroup(@Param("preemptionMasterGroupId") String groupId);


    @Select("SELECT * FROM hera_host_group ")
    List<HeraHostGroup> getAllHostGroupList();

    @Insert("<script>" +
            "insert into hera_host_group" +
            "<trim prefix = '(' suffix = ')' suffixOverrides=',' >" +
            "<if test = 'name != null' >" +
            "name ," +
            "</if>" +
            "<if test='effective != null' >" +
            "effective ," +
            "</if>" +
            "<if test = 'description != null' >" +
            "description ," +
            "</if>" +
            "</trim>" +
            "values" +
            "<trim prefix = '(' suffix = ')' suffixOverrides=',' >" +
            "<if test = 'name != null' >" +
            "#{name,jdbcType=VARCHAR}," +
            "</if>" +
            "<if test='effective != null' >" +
            "#{effective, jdbcType=INTEGER} ," +
            "</if>" +
            "<if test = 'description != null' >" +
            "#{description, jdbcType=VARCHAR}," +
            "</if>" +
            "</trim>" +
            "</script>")
    Integer insertHostGroup(HeraHostGroup hostGroup);

    @Update("update hera_host_group " +
            "set name = #{name, jdbcType = VARCHAR}, " +
            "effective = #{effective, jdbcType = INTEGER}," +
            "description = #{description, jdbcType = VARCHAR} " +
            "where id = #{id, jdbcType=INTEGER}")
    Integer updateHostGroup(HeraHostGroup hostGroup);

    @Delete("delete from hera_host_group where id = #{id, jdbcType=INTEGER}")
    Integer deleteHostGroup(Integer id);
}
