package com.dfire.common.mapper;

import com.dfire.common.entity.ZeusJob;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 14:24 2017/12/30
 * @desc
 */
public interface ZeusJobMapper {

    @Select("SELECT * FROM ZEUS_JOB WHERE ID = #{id}")
    @Results({
            @Result(id=true, column="id", property = "id"),
            @Result(column="gmt_create", property = "gmtCreate")
    })
    ZeusJob findByName(@Param("id") int id) ;
}
