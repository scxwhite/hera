package com.dfire.common.mapper;

import com.dfire.common.entity.HeraGroup;
import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.model.HeraGroupBean;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:59 2018/4/17
 * @desc
 */
public interface HeraGroupMapper {

    int insert(HeraGroup heraJob);

    int delete(@Param("id") String id, @Param("updateBy") String updateBy);

    int update(HeraGroup heraJob);

    List<HeraGroup> getAll();


    @Select("select * from hera_group where id = #{id}")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "gmt_create", property = "gmtCreate")
    })
    public HeraGroup findById(@Param("id") String id);

    @Select("SELECT * FROM hera_group WHERE name = 'default' ")
    HeraGroup findGlobalGroup();
}
