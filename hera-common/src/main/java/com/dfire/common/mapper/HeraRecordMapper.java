package com.dfire.common.mapper;

import com.dfire.common.entity.HeraRecord;
import com.dfire.common.entity.model.TablePageForm;
import com.dfire.common.mybatis.HeraInsertLangDriver;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * desc:
 *
 * @author scx
 * @create 2019/06/10
 */
public interface HeraRecordMapper {

    @Insert("insert into hera_record (#{heraRecord})")
    @Lang(HeraInsertLangDriver.class)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(HeraRecord heraRecord);


    @Select("select * from hera_record order by id desc limit #{startPos},#{limit}")
    List<HeraRecord> selectByPage(@Param("startPos") Integer startPos,
                                  @Param("limit") Integer limit);

    @Select("select * from hera_record where gid = #{gid} order by id desc limit #{startPos},#{limit}")
    List<HeraRecord> selectByGid(@Param("startPos") Integer startPos,
                                 @Param("limit") Integer limit,
                                 @Param("gid") Integer gid);


    @Select("select count(1) from hera_record where log_id = #{logId}")
    Integer selectCountById(Integer logId);


    @Select("select * from hera_record  where log_id = #{logId} order by id desc limit #{startPos},#{limit}")
    List<HeraRecord> findPageByLogId(@Param("startPos") Integer startPos,
                                     @Param("limit") Integer limit,
                                     @Param("logId") Integer logId);

    @Select("select count(1) from hera_record")
    Integer allCount();

    @Select("select count(1) from hera_record  where gid = #{gid} ")
    Integer countByGid(Integer gid);
}
