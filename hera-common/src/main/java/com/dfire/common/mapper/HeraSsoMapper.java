package com.dfire.common.mapper;

import com.dfire.common.entity.HeraSso;
import com.dfire.common.entity.vo.HeraSsoVo;
import com.dfire.common.mybatis.HeraInsertLangDriver;
import com.dfire.common.mybatis.HeraSelectLangDriver;
import com.dfire.common.mybatis.HeraUpdateLangDriver;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * desc:
 *
 * @author scx
 * @create 2019/06/10
 */
public interface HeraSsoMapper {

    @Insert("insert into hera_sso (#{heraSso})")
    @Lang(HeraInsertLangDriver.class)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Integer insert(HeraSso heraSso);

    @Delete("delete from hera_sso where id = #{id}")
    Integer delete(Integer id);

    @Update("update hera_sso (#{heraSso}) where id = #{id}")
    @Lang(HeraUpdateLangDriver.class)
    Integer update(HeraSso heraSso);


    @Select("select * from hera_sso where id = #{id}")
    @Lang(HeraSelectLangDriver.class)
    HeraSso findById(Integer id);

    @Select("SELECT * FROM hera_sso WHERE name = #{name}")
    HeraSso findByName(String name);

    @Select("select count(1) from hera_sso where name = #{name}")
    Integer checkExistByName(String name);

    @Select("select id,name,gid,job_number,email,phone,is_valid from hera_sso")
    List<HeraSso> selectAll();

    @Update("update hera_sso set is_valid=#{is_valid},gmt_modified=#{op_time} where id = #{id}")
    Integer updateValid(@Param("id") Integer id,
                        @Param("is_valid") Integer val,
                        @Param("op_time") long opTime);

    @Select("select sso.id,sso.name,sso.email,user.name g_name from \n" +
            "(select id,name,email,gid from hera_sso where id = #{id} ) sso \n" +
            "left join hera_user user on  sso.gid = user.id")
    HeraSsoVo findSsoVoById(Integer id);
}
