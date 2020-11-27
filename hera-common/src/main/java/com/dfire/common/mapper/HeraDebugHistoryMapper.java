package com.dfire.common.mapper;

import com.dfire.common.entity.HeraDebugHistory;
import com.dfire.common.mybatis.HeraInsertLangDriver;
import com.dfire.common.mybatis.HeraListInLangDriver;
import com.dfire.common.mybatis.HeraSelectLangDriver;
import com.dfire.common.mybatis.HeraUpdateLangDriver;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午7:25 2018/4/16
 * @desc 开发中心脚本运行记录dao
 */
public interface HeraDebugHistoryMapper {


    @Insert("insert into hera_debug_history (#{heraDebugHistory})")
    @Lang(HeraInsertLangDriver.class)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(HeraDebugHistory heraDebugHistory);

    @Delete("delete from hera_debug_history where id = #{id}")
    int delete(@Param("id") Long id);

    @Update("update hera_debug_history (#{heraDebugHistory}) where id = #{id}")
    @Lang(HeraUpdateLangDriver.class)
    int update(HeraDebugHistory heraDebugHistory);

    @Select("select * from hera_debug_history order by id desc")
    @Lang(HeraSelectLangDriver.class)
    List<HeraDebugHistory> getAll();

    @Select("select * from hera_debug_history where id = #{id}")
    @Lang(HeraSelectLangDriver.class)
    HeraDebugHistory findById(Long id);

    @Select("select * from hera_debug_history where id in (#{list})")
    @Lang(HeraListInLangDriver.class)
    List<HeraDebugHistory> findByIds(@Param("list") List<Integer> list);

    @Select("select * from hera_debug_history where file_id = #{fileId} order by id desc ")
    @Lang(HeraSelectLangDriver.class)
    List<HeraDebugHistory> findByFileId(Integer fileId);

    @Update("update hera_debug_history set status = #{status}, end_time = #{endTime} where id = #{id}")
    int updateStatus(HeraDebugHistory heraDebugHistory);

    @Update("update hera_debug_history set log = #{log}  where id = #{id}")
    int updateLog(HeraDebugHistory heraDebugHistory);

    @Select("select * from hera_debug_history where id = #{id}")
    HeraDebugHistory findLogById(Long id);

    @Update("update hera_debug_history set status=#{status},log=#{msg},end_time = #{endTime}  where id = #{id} ")
    void updateStatusAndLog(@Param("id") Long id,
                            @Param("msg") String msg,
                            @Param("status") String status,
                            @Param("endTime") String endTime);
}
