package com.dfire.common.mapper;

import com.dfire.common.entity.HeraGroup;
import com.dfire.common.mybatis.HeraInsertLangDriver;
import com.dfire.common.mybatis.HeraSelectInLangDriver;
import com.dfire.common.mybatis.HeraSelectLangDriver;
import com.dfire.common.mybatis.HeraUpdateLangDriver;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:59 2018/4/17
 * @desc
 */
public interface HeraGroupMapper {

    @Insert("insert into hera_group (#{heraGroup})")
    @Lang(HeraInsertLangDriver.class)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(HeraGroup heraGroup);

    @Update("update hera_group set existed = 0 where id = #{id}")
    int delete(@Param("id") int id);

    @Update("update hera_group (#{heraGroup}) where id = #{id}")
    @Lang(HeraUpdateLangDriver.class)
    int update(HeraGroup heraGroup);

    @Select("select * from hera_group")
    @Lang(HeraSelectLangDriver.class)
    List<HeraGroup> getAll();

    @Select("select * from hera_group where id = #{id} and existed = 1")
    @Lang(HeraSelectLangDriver.class)
    HeraGroup findById(HeraGroup heraGroup);

    @Select("select * from hera_group where id in (#{list}) and existed = 1")
    @Lang(HeraSelectInLangDriver.class)
    List<HeraGroup> findByIds(@Param("list") List<Integer> list);


    @Select("select * from hera_group where parent = #{parent} and existed = 1")
    @Lang(HeraSelectLangDriver.class)
    List<HeraGroup> findByParent(HeraGroup heraGroup);

    @Select("select * from hera_group where owner = #{owner} and existed = 1")
    @Lang(HeraSelectLangDriver.class)
    List<HeraGroup> findByOwner(HeraGroup heraGroup);



}
