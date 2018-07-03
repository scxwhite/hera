package com.dfire.common.mapper;


import com.dfire.common.entity.HeraPermission;
import com.dfire.common.mybatis.HeraInsertLangDriver;
import com.dfire.common.mybatis.HeraSelectInLangDriver;
import com.dfire.common.mybatis.HeraSelectLangDriver;
import com.dfire.common.mybatis.HeraUpdateLangDriver;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午3:49 2018/5/15
 * @desc
 */
public interface HeraPermissionMapper {

    @Insert("insert into hera_permission (#{heraPermission})")
    @Lang(HeraInsertLangDriver.class)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(HeraPermission heraPermission);

    @Delete("delete from hera_permission where id = #{id}")
    int delete(@Param("id") String id);

    @Update("update hera_permission (#{heraPermission}) where id = #{id}")
    @Lang(HeraUpdateLangDriver.class)
    int update(HeraPermission heraPermission);

    @Select("select * from hera_permission")
    @Lang(HeraSelectLangDriver.class)
    List<HeraPermission> getAll();

    @Select("select * from hera_permission where id = #{id}")
    @Lang(HeraSelectLangDriver.class)
    HeraPermission findById(HeraPermission heraPermission);

    @Select("select * from hera_permission where id in (#{list})")
    @Lang(HeraSelectInLangDriver.class)
    List<HeraPermission> findByIds(@Param("list") List<Integer> list);


}
