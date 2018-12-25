package com.dfire.common.mapper;

import com.dfire.common.entity.HeraUser;
import com.dfire.common.mybatis.HeraInsertLangDriver;
import com.dfire.common.mybatis.HeraListInLangDriver;
import com.dfire.common.mybatis.HeraSelectLangDriver;
import com.dfire.common.mybatis.HeraUpdateLangDriver;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 23:55 2017/12/29
 * @desc
 */
public interface HeraUserMapper {


    @Insert("insert into hera_user (#{heraUser})")
    @Lang(HeraInsertLangDriver.class)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(HeraUser heraUser);

    @Delete("delete from hera_user where id = #{id}")
    int delete(@Param("id") String id);

    @Update("update hera_user (#{heraUser}) where id = #{id}")
    @Lang(HeraUpdateLangDriver.class)
    int update(HeraUser heraUser);

    @Select("select * from hera_user")
    @Lang(HeraSelectLangDriver.class)
    List<HeraUser> getAll();

    @Select("select name from hera_user")
    @Lang(HeraSelectLangDriver.class)
    List<HeraUser> getAllName();

    @Select("select * from hera_user where id = #{id}")
    @Lang(HeraSelectLangDriver.class)
    HeraUser findById(HeraUser heraUser);

    @Select("SELECT * FROM hera_user WHERE NAME = #{name}")
    @Lang(HeraUpdateLangDriver.class)
    HeraUser getByName(HeraUser heraUser);

    @Select("select * from hera_user where id in (#{list})")
    @Lang(HeraListInLangDriver.class)
    List<HeraUser> findByIds(@Param("list") List<Integer> list);

    @Update("update hera_user set is_effective = #{isEffective} where id = #{id}")
    int updateEffective(@Param("id") String id, @Param("isEffective") String effective);


}
