package com.dfire.common.mapper;

import com.dfire.common.entity.HeraFile;

import java.util.List;

import com.dfire.common.mybatis.HeraInsertLangDriver;
import com.dfire.common.mybatis.HeraSelectInLangDriver;
import com.dfire.common.mybatis.HeraSelectLangDriver;
import com.dfire.common.mybatis.HeraUpdateLangDriver;
import org.apache.ibatis.annotations.*;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:54 2018/1/17
 * @desc 开发中心文件管理
 */
public interface HeraFileMapper {


    @Insert("insert into hera_file (#{heraFile})")
    @Lang(HeraInsertLangDriver.class)
    @Options(useGeneratedKeys = true, keyProperty = "heraFile.id", keyColumn = "id")
    int insert(HeraFile heraFile);

    @Delete("delete from hera_file where id = #{id}")
    int delete(@Param("id") String id);

    @Update("update hera_file (#{heraFile}) where id = #{id}")
    @Lang(HeraUpdateLangDriver.class)
    int update(HeraFile heraFile);

    @Select("select * from hera_file")
    @Lang(HeraSelectLangDriver.class)
    List<HeraFile> getAll();

    @Select("select * from hera_file where id = #{id}")
    @Lang(HeraSelectLangDriver.class)
    HeraFile findById(HeraFile heraFile);

    @Select("select * from hera_file where id in (#{list})")
    @Lang(HeraSelectInLangDriver.class)
    List<HeraFile> findByIds(@Param("list") List<Integer> list);


    @Select("select * from hera_file where parent = #{parent}")
    @Lang(HeraSelectLangDriver.class)
    List<HeraFile> findByParent(HeraFile heraFile);

    @Select("select * from hera_file where owner = #{owner}")
    @Lang(HeraSelectLangDriver.class)
    List<HeraFile> findByOwner(HeraFile heraFile);


}
