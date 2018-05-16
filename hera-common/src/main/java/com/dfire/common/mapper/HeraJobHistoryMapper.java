package com.dfire.common.mapper;

import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.vo.JobStatus;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午5:08 2018/4/23
 * @desc
 */
public interface HeraJobHistoryMapper {

    int insert(HeraJobHistory heraJobHistory);

    int delete(@Param("id") String id, @Param("updateBy") String updateBy);

    int update(HeraJobHistory heraJobHistory);

    List<HeraJobHistory> getAll();


    @Select("select * from hera_action_history where id = #{id}")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "gmt_create", property = "gmtCreate")
    })
    public HeraJobHistory findById(@Param("id") String id);

    @Select("select * from hera_action_history where jobId = #{jobId}")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "gmt_create", property = "gmtCreate")
    })
    public HeraJobHistory findByJobId(@Param("jobId") String jobId);

}
