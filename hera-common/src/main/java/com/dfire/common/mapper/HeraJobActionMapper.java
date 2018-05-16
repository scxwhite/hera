package com.dfire.common.mapper;

import com.dfire.common.entity.HeraAction;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午11:03 2018/5/16
 * @desc
 */
public interface HeraJobActionMapper {

    int insert(HeraAction heraAction);

    int delete(@Param("id") String id, @Param("updateBy") String updateBy);

    int update(HeraAction heraJobHistory);

    List<HeraAction> getAll();


    @Select("select * from hera_action where id = #{id}")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "gmt_create", property = "gmtCreate")
    })
    public HeraAction findById(@Param("id") String id);

    @Select("select * from hera_action where jobId = #{jobId}")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "gmt_create", property = "gmtCreate")
    })
    public HeraAction findByJobId(@Param("jobId") String jobId);


}
