package com.dfire.common.mapper;

import com.dfire.common.entity.HeraArea;
import com.dfire.common.mybatis.HeraInsertLangDriver;
import com.dfire.common.mybatis.HeraListInLangDriver;
import com.dfire.common.mybatis.HeraSelectLangDriver;
import com.dfire.common.mybatis.HeraUpdateLangDriver;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author xiaosuda
 * @date 2018/12/14
 */
public interface HeraAreaMapper {


    @Insert("insert into hera_area (#{heraArea})")
    @Lang(HeraInsertLangDriver.class)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Integer insert(HeraArea heraArea);


    @Select("select * from hera_area")
    @Lang(HeraSelectLangDriver.class)
    List<HeraArea> selectAll();


    @Update("update hera_area (#{heraArea}) where id = #{id}")
    @Lang(HeraUpdateLangDriver.class)
    int updateById(HeraArea heraArea);


    @Select("select * from hera_area where id = #{id}")
    @Lang(HeraSelectLangDriver.class)
    HeraArea selectById(Integer id);


    @Select("select * from hera_area where id in (#{list})")
    @Lang(HeraListInLangDriver.class)
    List<HeraArea> selectByIdList(@Param("list") List<Integer> list);

    @Select("delete from hera_area where id = #{id}")
    Integer deleteById(Integer id);
}
