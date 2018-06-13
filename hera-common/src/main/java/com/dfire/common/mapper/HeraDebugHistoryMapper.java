package com.dfire.common.mapper;

import com.dfire.common.entity.HeraDebugHistory;
import com.dfire.common.entity.HeraLock;
import com.dfire.common.entity.HeraPermission;
import com.dfire.common.mybatis.HeraInsertLangDriver;
import com.dfire.common.mybatis.HeraSelectInLangDriver;
import com.dfire.common.mybatis.HeraSelectLangDriver;
import com.dfire.common.mybatis.HeraUpdateLangDriver;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午7:25 2018/4/16
 * @desc
 */
public interface HeraDebugHistoryMapper {


    @Insert("insert into hera_debug_history (#{heraDebugHistory})")
    @Lang(HeraInsertLangDriver.class)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(HeraDebugHistory heraDebugHistory);

    @Delete("delete from hera_debug_history where id = #{id}")
    int delete(@Param("id") int id);

    @Update("update hera_debug_history (#{heraDebugHistory}) where id = #{id}")
    @Lang(HeraUpdateLangDriver.class)
    int update(HeraDebugHistory heraDebugHistory);

    @Select("select * from hera_debug_history")
    @Lang(HeraSelectLangDriver.class)
    List<HeraDebugHistory> getAll();

    @Select("select * from hera_debug_history where id = #{id}")
    @Lang(HeraSelectLangDriver.class)
    HeraDebugHistory findById(HeraDebugHistory heraDebugHistory);

    @Select("select * from hera_debug_history where id in (#{list})")
    @Lang(HeraSelectInLangDriver.class)
    List<HeraDebugHistory> findByIds(@Param("list") List<Integer> list);

    @Select("select * from hera_debug_history where file_id = #{fileId} ")
    @Lang(HeraSelectLangDriver.class)
    List<HeraDebugHistory> findByFileId(HeraDebugHistory heraDebugHistory);

    @Update("update hera_debug_history set status = #{status}, end_time = #{endTime} where id = #{id}")
    int updateStatus(HeraDebugHistory heraDebugHistory);

    @Update("update hera_debug_history set log = #{log}  where id = #{id}")
    int updateLog(HeraDebugHistory heraDebugHistory);




}
