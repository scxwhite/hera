package com.dfire.common.mapper;

import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.Judge;
import com.dfire.common.mybatis.HeraInsertLangDriver;
import com.dfire.common.mybatis.HeraListInLangDriver;
import com.dfire.common.mybatis.HeraSelectLangDriver;
import com.dfire.common.mybatis.HeraUpdateLangDriver;
import org.apache.ibatis.annotations.*;

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

    @Delete("update hera_job set is_valid = 0 where id = #{id}")
    int delete(@Param("id") int id);

    @Update("update hera_job (#{heraJob}) where id = #{id}")
    @Lang(HeraUpdateLangDriver.class)
    int update(HeraJob heraJob);

    @Select("select * from hera_job")
    @Lang(HeraSelectLangDriver.class)
    List<HeraJob> getAll();


    @Select("select id,group_id,name,owner from hera_job")
    @Lang(HeraSelectLangDriver.class)
    List<HeraJob> selectAll();

    @Select("select * from hera_job where id = #{id}")
    @Lang(HeraSelectLangDriver.class)
    HeraJob findById(Integer id);

    @Select("select * from hera_job where id in (#{list})")
    @Lang(HeraListInLangDriver.class)
    List<HeraJob> findByIds(@Param("list") List<Integer> list);

    @Select("select * from hera_job where group_id = #{groupId}")
    List<HeraJob> findByPid(Integer groupId);


    @Update("update hera_job set auto = #{status} where id = #{id}")
    Integer updateSwitch(@Param("id") Integer id, @Param("status") Integer status);


    @Select("select max(id) from hera_job")
    Integer selectMaxId();

    @Select("select `name`,id,dependencies,auto from hera_job")
    List<HeraJob> getAllJobRelations();

    @Select("select count(*) count, max(id) maxId, max(gmt_modified) lastModified from hera_job")
    Judge selectTableInfo();


    @Update("update hera_job set group_id = #{parentId} where id = #{newId}")
    Integer changeParent(@Param("newId") Integer newId, @Param("parentId") Integer parentId);

    @Select("select repeat_run from hera_job where id = #{jobId}")
    Integer findRepeat(Integer jobId);

    @Update("update hera_job set script= #{script} where id = #{id}")
    Integer updateScript(@Param("id") Integer id, @Param("script") String script);

    @Select("select must_end_minute from hera_job where id = #{id}")
    Integer findMustEndMinute(int id);

    @Select("select id,name, estimated_end_hour from hera_job where auto > 0 and is_valid > 0 and estimated_end_hour > 0 and estimated_end_hour >= #{startTime} and estimated_end_hour < #{endTime}")
    List<HeraJob> findEstimatedEndHours(@Param("startTime") int startTime,
                                        @Param("endTime") int endTime);
}
