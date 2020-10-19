package com.dfire.common.mapper;

import com.dfire.common.entity.HeraRerun;
import com.dfire.common.mybatis.HeraInsertLangDriver;
import com.dfire.common.mybatis.HeraUpdateLangDriver;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author xiaosuda
 * @date 2018/12/14
 */
public interface HeraRerunMapper {


    String COLUMN = " id,is_end,action_now,job_id,name,start_millis,end_millis,sso_name,extra,gmt_create ";
    String TABLE = " hera_rerun ";

    @Insert("insert into " + TABLE + " (#{heraRerun})")
    @Lang(HeraInsertLangDriver.class)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Integer insert(HeraRerun heraRerun);


    @Select("select " + COLUMN + " from " + TABLE + "order by id desc limit #{startPos},#{limit} ")
    List<HeraRerun> selectAll(@Param("startPos") Integer startPos,
                              @Param("limit") Integer limit);


    @Update("update " + TABLE + " (#{heraRerun}) where id = #{id}")
    @Lang(HeraUpdateLangDriver.class)
    Integer updateById(HeraRerun heraRerun);


    @Select("select " + COLUMN + " from " + TABLE + " where id = #{id}")
    HeraRerun selectById(Integer id);


    @Select("delete from " + TABLE + " where id = #{id}")
    Integer deleteById(Integer id);

    @Select("select " + COLUMN + "from " + TABLE + "where is_end =#{isEnd}")
    List<HeraRerun> selectByEnd(int isEnd);


    @Select("select " + COLUMN + "from" + TABLE + "where job_id=#{jobId} and is_end =#{isEnd} and start_millis <= #{millis} and end_millis >=#{millis} limit 1")
    HeraRerun selectRecordByTime(@Param("millis") Long millis,
                                 @Param("jobId") Integer jobId,
                                 @Param("isEnd") int isEnd);

    @Select("select " + COLUMN + "from" + TABLE + " where job_id=#{jobId} and is_end =#{isEnd} limit 1")
    HeraRerun selectByIdAndEnd(@Param("jobId") Integer jobId,
                               @Param("isEnd") int isEnd);

    @Select("select count(1) from " + TABLE)
    Integer selectCount();

    @Select("select count(1) from " + TABLE + "where job_id=#{jobId} and is_end = #{isEnd}")
    Integer selectCountByJob(@Param("jobId") Integer jobId,
                             @Param("isEnd") int isEnd);


    @Select("select " + COLUMN + " from " + TABLE + " where is_end = #{status} order by id desc limit #{startPos},#{limit} ")
    List<HeraRerun> selectAllByStatus(@Param("startPos") Integer startPos,
                                      @Param("limit") Integer limit,
                                      @Param("status") Integer status);


    @Select("select count(1) from " + TABLE + " where is_end = #{status}")
    Integer selectCountByStatus(Integer status);
}
