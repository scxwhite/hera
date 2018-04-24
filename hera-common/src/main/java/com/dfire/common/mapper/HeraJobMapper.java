package com.dfire.common.mapper;

import com.dfire.common.entity.HeraJob;
import org.apache.ibatis.annotations.*;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 14:24 2017/12/30
 * @desc
 */
public interface HeraJobMapper {

    @Select("SELECT * FROM hera_job WHERE ID = #{id}")
    @Results({
            @Result(id=true, column="id", property = "id"),
            @Result(column="gmt_create", property = "gmtCreate")
    })
    HeraJob findByName(@Param("id") int id) ;

    @Select("SELECT * FROM hera_job WHERE ID = #{jobId}")
    @Results({
            @Result(id=true, column="id", property = "id"),
            @Result(column="gmt_create", property = "gmtCreate")
    })
    HeraJob findById(@Param("jobId") String jobId) ;
}
