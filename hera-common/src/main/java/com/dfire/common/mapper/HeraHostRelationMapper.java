package com.dfire.common.mapper;

import com.dfire.common.entity.HeraHostRelation;
import com.dfire.common.mybatis.HeraInsertLangDriver;
import com.dfire.common.mybatis.HeraSelectLangDriver;
import com.dfire.common.mybatis.HeraUpdateLangDriver;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 17:05 2018/1/12
 * @desc
 */
public interface HeraHostRelationMapper {

    @Insert("insert into hera_host_relation (#{heraHostRelation})")
    @Lang(HeraInsertLangDriver.class)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(HeraHostRelation heraHostRelation);

    @Update("delete from  hera_host_relation where id = #{id}")
    int delete(@Param("id") int id);

    @Update("update hera_host_relation (#{heraHostRelation}) where id = #{id}")
    @Lang(HeraUpdateLangDriver.class)
    int update(HeraHostRelation heraHostRelation);

    @Select("select * from hera_host_relation")
    @Lang(HeraSelectLangDriver.class)
    List<HeraHostRelation> getAll();

    @Select("select * from hera_host_relation where id = #{id}")
    @Lang(HeraSelectLangDriver.class)
    HeraHostRelation findById(HeraHostRelation heraHostRelation);

    @Select("select host from hera_host_relation where host_group_id = #{groupId}")
    @Lang(HeraSelectLangDriver.class)
    List<String> findPreemptionGroup(@Param("groupId") int groupId);

}
