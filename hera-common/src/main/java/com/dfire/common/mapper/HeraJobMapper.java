package com.dfire.common.mapper;

import com.dfire.common.entity.HeraJob;
import org.apache.ibatis.annotations.*;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 14:24 2017/12/30
 * @desc
 */
public interface HeraJobMapper {

    @Select("SELECT * FROM HERA_JOB WHERE ID = #{id}")
    @Results({
            @Result(id=true, column="id", property = "id"),
            @Result(column="gmt_create", property = "gmtCreate")
    })
    HeraJob findByName(@Param("id") int id) ;
}
