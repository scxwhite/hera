package com.dfire.common.mapper;

import com.dfire.common.entity.HeraJob;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 14:24 2017/12/30
 * @desc
 */
public interface HeraJobMapper {



    int insert(HeraJob heraJob);

    int delete(@Param("id") String id, @Param("updateBy") String updateBy);

    int update(HeraJob heraJob);

    List<HeraJob> getAll();


    @Select("select * from hera_job where id = #{id}")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "gmt_create", property = "gmtCreate")
    })
    public HeraJob findById(@Param("id") String id);


}
