package com.dfire.common.mapper;

import com.dfire.common.entity.HeraJob;
import com.dfire.common.mybatis.HeraInsertLangDriver;
import com.dfire.common.mybatis.HeraSelectInLangDriver;
import com.dfire.common.mybatis.HeraSelectLangDriver;
import com.dfire.common.mybatis.HeraUpdateLangDriver;
import graph.JobRelation;
import org.apache.ibatis.annotations.*;
import org.omg.CORBA.INTERNAL;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 14:24 2017/12/30
 * @desc
 */
public interface HeraJobMapper {


    @Insert("insert into hera_job (#{heraJob})")
    @Lang(HeraInsertLangDriver.class)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(HeraJob heraJob);

    @Delete("delete from hera_job where id = #{id}")
    int delete(@Param("id") int id);

    @Update("update hera_job (#{heraJob}) where id = #{id}")
    @Lang(HeraUpdateLangDriver.class)
    Integer update(HeraJob heraJob);

    @Select("select * from hera_job")
    @Lang(HeraSelectLangDriver.class)
    List<HeraJob> getAll();

    @Select("select * from hera_job where id = #{id}")
    @Lang(HeraSelectLangDriver.class)
    HeraJob findById(HeraJob heraJob);

    @Select("select * from hera_job where id in (#{list})")
    @Lang(HeraSelectInLangDriver.class)
    List<HeraJob> findByIds(@Param("list") List<Integer> list);

    @Select("select * from hera_job where group_id = #{groupId}")
    @Lang(HeraSelectLangDriver.class)
    List<HeraJob> findByPid(HeraJob heraJob);


    @Update("update hera_job set auto = !auto where id = #{id}")
    Integer updateSwitch(Integer id);



    @Select("select id,dependencies from hera_job where schedule_type = 1")
    List<HeraJob> getAllJobRelation();


    @Select("select max(id) from hera_job")
    Integer selectMaxId();

    @Select("select `name`,id,dependencies from hera_job where auto=1")
    List<JobRelation> getJobRelations();
}
